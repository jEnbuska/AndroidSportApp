package com.pepster;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.pepster.views.ForgotPasswordFragment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Laemmi on 21.3.2016.
 */
public class LoginActivity extends AppCompatActivity implements ForgotPasswordFragment.PassEmailListener {

    public final static String AFTER_LOGIN = "LoginActivity.Login";

    private Firebase mFirebase;
    private AuthData mAuthData;
    private Firebase.AuthStateListener mAuthStateListener;
    private LoginButton mFbLoginButton;
    private CallbackManager mFbCallbackManager;
    private AccessTokenTracker mFbAccessTokenTracker;
    private Button mPasswordLoginButton;
    private EditText mUsername;
    private EditText mPassword;
    private Button mForgotPassword;
    private ForgotPasswordFragment mForgotPasswordFragment;
    private String email;

    SharedPreferences mSharedPreference;
    SharedPreferences.Editor editor;
    private final String TAG = LoginActivity.class.getSimpleName();
    private  final String USERPREFERENCE = "com.pepster.USER";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSharedPreference = getApplicationContext().getSharedPreferences(USERPREFERENCE, Context.MODE_PRIVATE);
        editor = mSharedPreference.edit();
        mFirebase = new Firebase("https://glaring-fire-3708.firebaseio.com");

        mFbCallbackManager = CallbackManager.Factory.create();

        mFbAccessTokenTracker = new AccessTokenTracker() {

            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                Log.i(TAG, "Facebook.AccessTokenTracker.OnCurrentAccesTokenChanged");
                LoginActivity.this.onFacebookAccessTokenChange(currentAccessToken);
            }

        };
        Log.i(TAG, "accestoken tracker created");
        mForgotPassword = (Button) findViewById(R.id.forgot_password);
        mFbLoginButton = (LoginButton) findViewById(R.id.login_with_facebook);
        mPasswordLoginButton = (Button) findViewById(R.id.login_with_password);
        mUsername = (EditText) findViewById(R.id.txtUsername);
        mPassword = (EditText) findViewById(R.id.txtPassword);


        mPasswordLoginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                loginWithPassword(mUsername.getText().toString(), mPassword.getText().toString());

            }
        });

        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Forgot password pressed");
                mForgotPasswordFragment = ForgotPasswordFragment.newInstance();
                /*getSupportFragmentManager().beginTransaction()
                        .add(R.id.login_fragment_holder, mForgotPasswordFragment).show(mForgotPasswordFragment)
                        .commit();*/
                mForgotPasswordFragment.show(getSupportFragmentManager(),"forgot_password_dialog");
            }
        });


        /*mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Loading");
        mAuthProgressDialog.setMessage("Authenticating...");
        mAuthProgressDialog.setCancelable(false);
        mAuthProgressDialog.show();*/

        mAuthStateListener = new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                //mAuthProgressDialog.hide();
                Log.i(TAG, "AuthStateChanged");
                setAuthenticatedUser(authData);
            }
        };
        mFirebase.addAuthStateListener(mAuthStateListener);
        //TODO this line is for debug (autologin)
        loginWithPassword("test@testt.com", "test1234");
    }
    private void authWithFirebase(final String provider, Map<String, String> options){
        if(options.containsKey("error")){
            Log.i(TAG, "Error authenticating");
        } else {
            //mAuthProgressDialog.show();
            mFirebase.authWithOAuthToken(provider, options.get("oauth_token"), new AuthResultHandler(provider));
        }
    }


    private void setAuthenticatedUser(AuthData authData) {
        //Log.i(TAG, "setAUth  "+(String)authData.getUid());//+authData.getUid().toString());
        if (authData != null) {
            Log.i(TAG, "setAUth  "/*+authData.getUid().toString()*/);
            String name = null;
            if (authData.getProvider().equals("facebook")) {
                Log.i(TAG, "HELLO AT FACEBOOK LOGIN");
                name = (String) authData.getProviderData().get("displayName");
            } else if (authData.getProvider().equals("password")) {
                name = authData.getUid();
            } else {
                Log.e(TAG, "invalid provider: " + authData.getProvider());
            }
            if (name != null) {
                editor.putString(USERPREFERENCE, (String) authData.getUid());
                editor.apply();
                SharedPreferences provider = getApplicationContext().getSharedPreferences("PROVIDER", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor2 = provider.edit();
                editor2.putString("PROVIDER", (String) authData.getProvider());
                editor2.apply();
                Log.i(TAG, "Starting main activity " + mSharedPreference.getString(USERPREFERENCE, null));
                Log.i(TAG, "user authdata " + (String)authData.getUid());
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra(AFTER_LOGIN, true);
                startActivity(i);
                finish();
                Log.e(TAG, "login activity still running");
            }

            this.mAuthData = authData;

        }
    }

    @Override
    protected void onDestroy() {
        if(mForgotPasswordFragment!=null)
            mForgotPasswordFragment.dismiss();
        super.onDestroy();

        Log.i(TAG, "onDestroy kutsu");



    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "F1");
        mFbCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private class AuthResultHandler implements Firebase.AuthResultHandler {

        private  final String provider;
        public AuthResultHandler(String provider) {
            this.provider = provider;
        }

        @Override
        public void onAuthenticated(AuthData authData) {
            //mAuthProgressDialog.hide();
            Log.i(TAG, provider + " auth ok");
            Map<String, String> map = new HashMap<String, String>();
            map.put("provider", authData.getProvider());
            if(authData.getProviderData().containsKey("displayName")) {
                map.put("displayName", authData.getProviderData().get("displayName").toString());
            } else if(authData.getProviderData().containsKey("email")) {
                map.put("email", authData.getProviderData().get("email").toString());
            }
            //mFirebase.child("users").child(authData.getUid()).setValue(map);
            setAuthenticatedUser(authData);
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError){
            //mAuthProgressDialog.hide();
            Log.e(TAG, firebaseError.toString() + " Loggausvaihe");
            Toast.makeText(LoginActivity.this, R.string.no_such_email_message, Toast.LENGTH_LONG).show();
        }
    }

    private class ResultHandler implements Firebase.ResultHandler {


        @Override
        public void onSuccess() {

            Toast.makeText(LoginActivity.this,"Email sent", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(FirebaseError firebaseError) {
            Toast.makeText(LoginActivity.this, "Account with the given email not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void onFacebookAccessTokenChange(AccessToken token) {
        if(token != null) {
            //AuthProgressDialog.show();
            Log.i(TAG, "onfacebookhommeli ok");
            mFirebase.authWithOAuthToken("facebook", token.getToken(), new AuthResultHandler("facebook"));
        } else {
            if (this.mAuthData != null && this.mAuthData.getProvider().equals("facebook")){
                mFirebase.unauth();
                Log.i(TAG, "onfacebookhommeli");
                setAuthenticatedUser(null);
            }
        }
    }

    public void loginWithPassword(String us, String psw) {
        final String userName = us;
        final String pw = psw;

        //mAuthProgressDialog.show();
        mFirebase.authWithPassword(userName, pw, new AuthResultHandler("password"));
    }

    @Override
    public void passEmail(String email){
        this.email = email;
        resetForgottenPassword(email);
    }
    public void resetForgottenPassword(String s){

        mFirebase.resetPassword(s, new ResultHandler());
    }
}

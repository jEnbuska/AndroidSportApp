package com.pepster;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.pepster.data.MapContent;
import com.pepster.data.PepPoint;
import com.pepster.utilities.AdaptiveLocation;
import com.pepster.utilities.Sorter;
import com.pepster.views.MapListFragment;
import com.pepster.views.MapViewFragment;

import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

import static com.pepster.views.MapViewFragment.MODE_RECORD;
import static com.pepster.views.MapViewFragment.MODE_WALK;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 15;
    public final static String
            DUMMY_ROUTES = "dummy_routes",
            DUMMY_ROUTES_VOL_2 = "dummy_routes_vol_2",
            SETTINGS = "SETTINGS",
            USERS = "users",
            PEP_POINTS = "pepPoints",
            MOD_TIME = "modTime",
            APP = "RoutePepper_APP",
            USERPREFERENCE = "com.pepster.USER",
            TAG = MainActivity.class.getSimpleName();

    private static BehaviorSubject<Location> mLocationSubject;
    public static Vibrator vibrator;
    private MapListFragment mListFragment;
    private GoogleApiClient mGoogleApiClient;
    private Sorter<MapContent> mSorter;
    private LocationRequest mLocationRequest;
    private ChildEventListener mChildEventListener;
    private MapContent mSelectedContent;
    private MapViewFragment mMapViewFragment;
    private MapRecyclerViewAdapter mRecyclerViewAdapter;
    private String [] comparatorOptions;
    private Firebase mFirebase;
    private Firebase mFirebaseRoutesRef;
    private String uId;
    private Subscription mFireBaseUpdater;

    private String mNewRouteName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Firebase.setAndroidContext(this);
        if(!mFirebase.getDefaultConfig().isPersistenceEnabled())
            Firebase.getDefaultConfig().setPersistenceEnabled(true);

        FacebookSdk.sdkInitialize(getApplicationContext());
        if(getSharedPreferences(USERPREFERENCE, MODE_PRIVATE).getString(USERPREFERENCE, null)==null){
            Intent intent = new Intent (getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            PepPoint.initContext(this);
            mLocationSubject = BehaviorSubject.create();
            mLocationSubject.subscribe(v -> Log.i(TAG, "location change " + v));
            //supportInvalidateOptionsMenu();
            uId = getSharedPreferences(USERPREFERENCE, MODE_PRIVATE).getString(USERPREFERENCE, null);
            mFirebase = new Firebase("https://glaring-fire-3708.firebaseio.com");
            comparatorOptions = getResources().getStringArray(R.array.comparators);
            initSorter();
            mRecyclerViewAdapter = new MapRecyclerViewAdapter(mSorter);
            mListFragment = MapListFragment.newInstance(mRecyclerViewAdapter, new MapListFragment.SpinnerSelectionListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mSorter.setState(comparatorOptions[position]);
                    mRecyclerViewAdapter.notifyDataSetChanged();//listadapter ??SHOULD?? internally sort the items in the map
                }
            });
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_holder, mListFragment)
                    .commit();

            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            comparatorOptions = getResources().getStringArray(R.array.comparators);

            if (mGoogleApiClient == null) {
                // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
                // See https://g.co/AppIndexing/AndroidStudio for more information.
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
            }
            initChildEventListener();
            mFirebaseRoutesRef = mFirebase.child(USERS).child(uId).child(DUMMY_ROUTES_VOL_2);
            mFirebaseRoutesRef.keepSynced(true);
            mFirebaseRoutesRef.orderByKey().addChildEventListener(mChildEventListener);

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, R.string.storage_access_request_message, Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private void initChildEventListener(){
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                MapContent content = MapContent.create(dataSnapshot);
                mRecyclerViewAdapter.add(content);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                MapContent corresponding = mRecyclerViewAdapter
                        .get(mapContent -> mapContent.ID().equals(dataSnapshot.getKey()));
                if(corresponding.getModTime()<(Long)dataSnapshot.child(MOD_TIME).getValue()) {
                    if(mFireBaseUpdater!=null)
                        /*unsubscribe so that you will no get updated about the changes that are due
                        to calling 'updateWith' (lines below)*/
                        mFireBaseUpdater.unsubscribe();
                    corresponding.updateWith(MapContent.create(dataSnapshot));
                    Observable.timer(2, TimeUnit.SECONDS)
                            .subscribe(n -> subscribeToContentUpdates());//subscribe back
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mRecyclerViewAdapter.currentContentObservable()
                        .filter(mapContent -> mapContent.ID().equals(dataSnapshot.getKey()))
                        .subscribe(mapContent -> {
                            mapContent.propagateUnSubscribe();
                            mRecyclerViewAdapter.remove(mapContent);
                        });
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, "onChildMoved " + dataSnapshot);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.i(TAG, "onCancelled " + firebaseError);
            }
        };
    }

    /** Call to this method is defined in xml file recycler_view_listitem
     * MapContent is tagger to the view inside MapRecyclerViewAdapter:s onBindViewHolder method*/
    public void onMapListItemClick(View view) {
        mSelectedContent = (MapContent) view.getTag();
        openMapView(MODE_WALK);
    }

    /**creates and open the mapview fragment in the specified MODE */
    private void openMapView(int MODE){
        subscribeToContentUpdates();
        mSelectedContent.subscribeToLocationChangeUpdates(); //fire message if user in within a peppoints range
        mMapViewFragment = MapViewFragment.newInstance(mSelectedContent, MODE);
        getFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .hide(mListFragment)
                .add(R.id.fragment_holder, mMapViewFragment)
                .commit();
    }

    /**is called when a MapContent is selectet from the list
     * No when user modifies the MapConents data all the updates will be saved automatically to
     * firebase*/
    private void subscribeToContentUpdates(){
        try{
            Firebase contentFirebaseRef = mFirebaseRoutesRef.child(mSelectedContent.ID());
            mFireBaseUpdater = mSelectedContent.updateObservable()
                    .subscribeOn(Schedulers.computation())
                    .debounce(3, TimeUnit.SECONDS)
                    .distinctUntilChanged()
                    .doOnNext(v -> mSelectedContent.setModTime(System.currentTimeMillis()))
                    .doOnNext(v -> Log.i(APP, v))
                    .subscribe(obj -> contentFirebaseRef.setValue(mSelectedContent));
        }catch (NullPointerException e){
            Log.v(APP, "NullPointerException in subscribeToContentUpdates");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        /*Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.pepster/http/host/path")
        );
        AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);*/
    }

    @Override
    protected void onDestroy() {
        if(mGoogleApiClient!=null)
            mGoogleApiClient.disconnect();
        PepPoint.shutDownTTS();
        if(mFireBaseUpdater !=null && !mFireBaseUpdater.isUnsubscribed())
            mFireBaseUpdater.unsubscribe();
        if(mLocationSubject!=null)
            mLocationSubject.onCompleted();
        if(mRecyclerViewAdapter!=null)
            mRecyclerViewAdapter.currentContentObservable()
                    .forEach(content -> content.propagateUnSubscribe());
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        if(uId != null) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        } else {
            return false;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        if(mFirebase.getAuth()!=null) {
            mFirebase.unauth();
        }
        LoginManager.getInstance().logOut();
        SharedPreferences.Editor edit = getApplicationContext().getSharedPreferences(USERPREFERENCE, Context.MODE_PRIVATE).edit();
        edit.putString(USERPREFERENCE, "");
        edit.remove(USERPREFERENCE);
        edit.apply();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnected(Bundle bundle) {
        //ask for privileges if not granted yet
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 22);
        }else {
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setInterval(2000);
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setSmallestDisplacement(1/*Meters*/);

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed " + connectionResult.toString());
    }

    @Override
    public void onLowMemory() {
        Log.i(TAG, "onLowMemory");
        super.onLowMemory();
    }

    public static Observable<Location> currentLocationObservable(){
        return mLocationSubject.asObservable().doOnNext(loc -> Log.i(TAG, "location changed"));
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocationSubject.onNext(location);
    }

    @Override
    public void onBackPressed() {
        if(mListFragment.isVisible()){
            super.onBackPressed();
        }else if(mMapViewFragment.isVisible()){
            getFragmentManager().popBackStack();
            mSelectedContent.unSubscribeLocationChangeUpdates();
            mSelectedContent.resetToDefault();
        }
    }

    public void onRecordClicked(View v){
        mSelectedContent = new MapContent();
        mSelectedContent.setTitle(Calendar.getInstance().getTime().toString());
        mSelectedContent.setLastUsage(System.currentTimeMillis());
        mSelectedContent.setModTime(System.currentTimeMillis());
        openMapView(MODE_RECORD);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                finish();
            }
        }
    }

    //currently out of use
    private void initSorter() {
        Map<String, Comparator<MapContent>> comparators = new HashMap<>();
        String alphabetical = getString(R.string.alphabetical_comparator);
        mSorter = new Sorter<>(comparators, alphabetical);

        comparators.put(alphabetical, (lhs, rhs) -> lhs.getTitle().compareTo(rhs.getTitle()));
        comparators.put(getString(R.string.recently_used_comparator), (lhs, rhs) -> {
            if (lhs.getLastUsage() < rhs.getLastUsage()) {
                return -1;
            } else if (lhs.getLastUsage() > rhs.getLastUsage()) {
                return 1;
            } else {
                return 0;
            }
        });
        comparators.put(getString(R.string.recently_modified_comparator), (lhs, rhs) -> {
            if (lhs.getModTime() < rhs.getModTime()) {
                return -1;
            } else if (lhs.getModTime() > rhs.getModTime()) {
                return 1;
            } else {
                return 0;
            }
        });
        comparators.put(getString(R.string.closest_comparator), (lhs, rhs) -> {
            try {
                try {
                    AdaptiveLocation lhsStart = lhs.route().get(0);
                    Location lhsLocation = new Location("lhs");
                    lhsLocation.setLatitude(lhsStart.getLat());
                    lhsLocation.setLongitude(lhsStart.getLng());

                    AdaptiveLocation rhsStart = rhs.route().get(0);
                    Location rhsLocation = new Location("rhs");
                    rhsLocation.setLatitude(rhsStart.getLat());
                    rhsLocation.setLongitude(rhsStart.getLng());

                    double distanceToRhs = mLocationSubject.getValue().distanceTo(rhsLocation);
                    double distanceToLhs = mLocationSubject.getValue().distanceTo(lhsLocation);
                    if (distanceToRhs < distanceToLhs) {
                        return 1;
                    } else {
                        return -1;
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "mLocationManager not implemented?", Toast.LENGTH_LONG).show();
                }
            } catch (SecurityException e) {
                Toast.makeText(MainActivity.this, getString(R.string.user_security_remark), Toast.LENGTH_LONG).show();
            }
            return 0;
        });
    }
}

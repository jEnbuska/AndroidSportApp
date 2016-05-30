package com.pepster.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.widget.EditText;

/**
 * Created by Laemmi on 23.3.2016.
 */
public class ForgotPasswordFragment extends AppCompatDialogFragment {
    private PassEmailListener pass;

    public static ForgotPasswordFragment newInstance() {;
        ForgotPasswordFragment f = new ForgotPasswordFragment();
        return f;
    }
    public ForgotPasswordFragment(){

    }

    @Override
    public void onAttach(Activity a){
        super.onAttach(a);
        try {
           pass = (PassEmailListener) a;
        } catch (ClassCastException e) {
            throw new ClassCastException(a.toString() + " must implement PassEmailListener");
        }
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final EditText input = new EditText(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(input);
        builder.setTitle("Forgot password")
                .setPositiveButton("Send email", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pass.passEmail(input.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               dialog.cancel();
            }
        });

        return builder.create();
    }

    public interface PassEmailListener {
        public void passEmail(String s);
    }
}

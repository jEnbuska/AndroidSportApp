package com.pepster.Interfaces;

import android.text.TextWatcher;
import android.util.Log;

/**
 * Created by WinNabuska on 27.3.2016.
 *
 */
public abstract class AfterTextChangeListener implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }
}

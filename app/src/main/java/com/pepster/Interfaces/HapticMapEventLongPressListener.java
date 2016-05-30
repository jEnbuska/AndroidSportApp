package com.pepster.Interfaces;

import android.content.Context;
import android.view.MotionEvent;

import com.pepster.MainActivity;

import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

/**
 * Created by WinNabuska on 1.4.2016.
 */
public abstract class HapticMapEventLongPressListener implements MapEventsReceiver {
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        MainActivity.vibrator.vibrate(new long[]{50, 50, 50, 50}, -1);
        onLongPress(p);
        return false;
    }

    public abstract void onLongPress(GeoPoint p);
}

package com.pepster.utilities;

import org.osmdroid.bonuspack.overlays.Marker;

/**
 * Created by WinNabuska on 30.3.2016.
 */
public class MarkerDragListener implements Marker.OnMarkerDragListener {

    private OnMarkerDragContinueListener dragListener;
    private OnMarkerDragStartListener dragStartListener;

    public MarkerDragListener(OnMarkerDragStartListener dragStartListener, OnMarkerDragContinueListener dragListener){
        this.dragStartListener=dragStartListener;
        this.dragListener=dragListener;
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        dragListener.onMarkerDrag(marker);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        dragStartListener.onMarkerDragStart(marker);
    }

    public static interface OnMarkerDragStartListener {
        void onMarkerDragStart(Marker marker);
    }

    public static interface OnMarkerDragContinueListener {
        void onMarkerDrag(Marker marker);
    }
}

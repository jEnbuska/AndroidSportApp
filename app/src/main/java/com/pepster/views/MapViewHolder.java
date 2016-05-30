package com.pepster.views;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.pepster.R;
import com.pepster.data.MapContent;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MapViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {

    private final String TAG = MapViewHolder.class.getSimpleName();
    private TextView mTitleTV;
    protected GoogleMap mGoogleMap;
    protected MapContent mContent;

    private CompositeSubscription mSubscriptions;

    public final MapView mMapView;
    private Context mContext;

    public MapViewHolder(Context context, View view) {
        super(view);
        mTitleTV = (TextView)view.findViewById(R.id.list_title);
        mSubscriptions = new CompositeSubscription();
        mContext = context;
        mMapView = (MapView) view.findViewById(R.id.list_map);
        mMapView.onCreate(null);
        mMapView.getMapAsync(this);
    }

    public void setContent(MapContent content) {
        mContent = content;
        if (mGoogleMap != null) {
            updateMapContents();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        MapsInitializer.initialize(mContext);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        // If we have map data, updateWith the map content.
        if (mContent != null) {
            updateMapContents();
        }
    }

    protected void updateMapContents() {
        mSubscriptions.add(mContent.titleObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(title -> mTitleTV.setText(title)));

        UiSettings settings = mGoogleMap.getUiSettings();
        //hide open googlemaps button
        settings.setMapToolbarEnabled(false);
        //disable gestures
        settings.setAllGesturesEnabled(false);

        mSubscriptions.add(mContent.route().observable()
                .debounce(30, TimeUnit.MILLISECONDS)
                .onBackpressureLatest()
                .filter(list -> list.size() > 1)
                .map(list -> (Observable.from(list)
                                .map(v -> v.asLatLng())
                                .toList()
                                .toBlocking()
                                .single()))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(road -> {
                    LatLngBounds.Builder boundBuilder = LatLngBounds.builder();
                    for (LatLng latLng : road) {
                        boundBuilder.include(latLng);
                    }
                    //double check the road size. Creating a road from too few points will crash the app
                    if(road.size() > 1) {
                        mGoogleMap.clear();
                        //Add start marker
                        mGoogleMap.addMarker(new MarkerOptions().position(road.get(0)));
                        //Add roud line to map
                        mGoogleMap.addPolyline(new PolylineOptions().addAll(road).color(Color.BLUE));
                        //zoom in and show route
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 3));
                        mMapView.invalidate();
                    }
                }));
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.i(TAG, "finalize");
        mSubscriptions.unsubscribe();
    }
}

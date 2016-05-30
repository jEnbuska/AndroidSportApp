package com.pepster.temp;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.pepster.data.MapContent;
import com.pepster.R;

import java.util.ArrayList;
import java.util.List;

import static com.annimon.stream.Collectors.*;

/**
 * Created by WinNabuska on 14.3.2016.
 */

//TODO poista tämä. Ei enää käytössä. Säilössä esimerkkejä varten
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private static final String TAG = MyAdapter.class.getName();
    private ArrayList<MapContent> mDataset;
    private Context mContext;

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(ArrayList<MapContent> myDataset, Context context) {
        mContext = context;
        mDataset = myDataset;
    }

    public void add(int position, MapContent item) {
        mDataset.add(position, item);
        notifyItemInserted(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder");
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_listitem, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder");
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final String name = mDataset.get(position).getTitle();
        holder.textView.setText(name);
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove(name);
            }
        });
        if(holder.googleMap!=null && !holder.drawn){
            drawMapContent(holder.googleMap, mDataset.get(position));
            holder.drawn=true;
        }else{
            Log.i(TAG, "MAP NULL");
        }
    }

    public void remove(String item) {
        int position = mDataset.indexOf(item);
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    private static void drawMapContent(GoogleMap googleMap, MapContent content) {
        //Set googleMap bounds
        new GoogleMapOptions().liteMode(true);
        LatLngBounds.Builder boundBuilder = LatLngBounds.builder();
        List<LatLng> routeCoords = Stream.of(content.getRoute()).map(sl -> new LatLng(sl.getLat(),sl.getLng())).collect(toList());
        for(LatLng location: routeCoords) {
            boundBuilder.include(location);
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 0);
        googleMap.animateCamera(cameraUpdate);
        UiSettings settings = googleMap.getUiSettings();

        //hide open googlemaps app button
        settings.setMapToolbarEnabled(false);
        settings.setAllGesturesEnabled(false);

        //Draw googleMap route
        PolylineOptions polylineOptions = new PolylineOptions().add(routeCoords.toArray(new LatLng[routeCoords.size()]));
        polylineOptions.color(Color.BLUE);
        googleMap.addPolyline(polylineOptions);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        new GoogleMapOptions().liteMode(true);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback{

        // each data item is just a string in this case
        public boolean drawn = false;
        public TextView textView;
        public MapView mapView;
        public GoogleMap googleMap;

        public ViewHolder(View v) {
            super(v);
            //TODO googlemapsoptions lite
            mapView = (MapView) v.findViewById(R.id.list_map);
            //TODO setclickablefalse?
            if (mapView != null) {
                // Initialise the MapView
                mapView.onCreate(null);
                // Set the map ready callback to receive the GoogleMap object
                mapView.getMapAsync(this);
            }
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(mContext);
            this.googleMap = googleMap;
        }
    }
}

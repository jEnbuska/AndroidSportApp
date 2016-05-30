package com.pepster.temp;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.annimon.stream.Collectors.*;

/**
 * Created by WinNabuska on 12.3.2016.
 */
//TODO poista tämä. Ei enää käytössä. Säilössä esimerkkejä varten
public class MapListAdapter  extends ArrayAdapter<MapContent> {

    private final static String TAG  = MapListAdapter.class.getSimpleName();
    private AbsListView.RecyclerListener mRecyclerListener;
    private Set<MapView> mMapViews;

    public MapListAdapter(Context context, ArrayList<MapContent> contents) {
        super(context, R.layout.recycler_view_listitem, contents);
        mMapViews=new HashSet<>();
        new GoogleMapOptions().liteMode(true);
    }

    public void initializeRecycleListener(ListView parent){
        parent.setRecyclerListener(mRecyclerListener = new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                ItemHolder holder = (ItemHolder) view.getTag();
                if (holder != null && holder.map != null) {
                    // Clear the map and free up resources by changing the map type to none
                    Log.i(TAG, "Clear map");
                    holder.map.clear();
                    holder.map.setMapType(GoogleMap.MAP_TYPE_NONE);
                }
            }
        });
    }

    @Override
    public void sort(Comparator<? super MapContent> comparator) {
        super.sort(comparator);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ItemHolder holder;
        final MapContent item = getItem(position);
        // Check if a view can be reused, otherwise inflate a layout and set up the view holder
        if (row == null) {
            // Inflate view from layout file
            row = null;//LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            // Set up holder and assign it to the View
            holder = new ItemHolder(getContext());
            holder.mapView = null;//(MapView) row.findViewById(R.id.list_item_lite_map);
            mMapViews.add(holder.mapView);
            //Disable googlemaps app opening when mapview is clicked
            holder.mapView.setClickable(false);
            holder.title = null;//(TextView) row.findViewById(R.id.map_title_tv);
            holder.title.setText(getItem(position).getTitle());// Set the text label for this item

            //pass the click events to itemselectlistener

            row.setTag(holder);
            holder.initializeMapView();

            // Set holder as tag for row for more efficient access.
            row.setTag(holder);
            // Keep track of MapView
        } else {
            // View has already been initialised, get its holder
            holder = (ItemHolder) row.getTag();
        }
        // Get the NamedLocation for this item and attach it to the MapView
        holder.mapView.setTag(item);
        // Get the NamedLocation for this item and attach it to the MapView
        if(holder.map!=null) {
            drawMapContent(holder.map, item);
        }
        holder.title.setText(item.getTitle());
        return row;
    }

    private static void drawMapContent(GoogleMap googleMap, MapContent content) {
        //Set googleMap bounds
        LatLngBounds.Builder boundBuilder = LatLngBounds.builder();
        List<LatLng> routeCoords = Stream.of(content.route().getList()).map(sl -> new LatLng(sl.getLat(),sl.getLng())).collect(toList());
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
    }

    public Set<MapView> getMapViews() {
        return mMapViews;
    }

    public class ItemHolder implements OnMapReadyCallback {
        MapView mapView;
        TextView title;
        GoogleMap map;
        private Context mContext;

        public ItemHolder(Context context){
            mContext=context;
        }
        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(mContext);
            map = googleMap;
            MapContent data = (MapContent) mapView.getTag();
            if (data != null) {
                MapListAdapter.drawMapContent(map, data);
            }
        }

        public void initializeMapView() {
            if (mapView != null) {
                // Initialise the MapView
                mapView.onCreate(null);
                // Set the map ready callback to receive the GoogleMap object
                mapView.getMapAsync(this);
            }
        }
    }
}

package com.pepster.views;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.pepster.data.Trio;
import com.pepster.utilities.AdaptiveLocation;
import com.pepster.MainActivity;
import com.pepster.R;
import com.pepster.data.MapContent;
import com.pepster.data.PepPoint;
import com.pepster.utilities.InPairs;
import com.pepster.utilities.RetryWithGrowingDelay;
import com.pepster.utilities.SkipWhen;
import com.pepster.Interfaces.HapticMapEventLongPressListener;
import com.pepster.utilities.MarkerDragListener;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MapViewFragment extends Fragment {

    private static Drawable nodeIcon;
    public static final int MODE_WALK = 1, MODE_DRAW = 2, MODE_RECORD = 3;
    private static final double MAX_DEVIATION = 7;
    private static final double REQUIRED_ACCURACY = 18;
    private final static String TAG = MapViewFragment.class.getSimpleName();
    private MapView mMapView;
    private MapContent mMapContent;

    /**subscribes to location change, MapContent route observable and peppoint observable*/
    private CompositeSubscription mMainSubscriptions;

    /**keeps track subscriptions and data that are bind to spesific PepPoint IDs
     * When a PepPoint is removed we can reference to this map and remove corresponding
     * map overlays and position subscription*/
    private Map<String, Trio<Marker,Polygon, Subscription>> mPepPointDatas;

    private int MODE;

    public static MapViewFragment newInstance(MapContent mapContent, int MODE){
        MapViewFragment fragment = new MapViewFragment();
        fragment.MODE=MODE;
        fragment.mMapContent = mapContent;
        return fragment;
    }

    public MapViewFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);

        mMainSubscriptions = new CompositeSubscription();

        mMapView = (MapView) view.findViewById(R.id.osmdroid_map);
        mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMaxZoomLevel(20);
        final IMapController mapController = mMapView.getController();
        mapController.setZoom(17);

        mMapView.setMultiTouchControls(true);
        final MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(getActivity(), mMapView);
        myLocationOverlay.enableFollowLocation();
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.setEnabled(true);
        myLocationOverlay.setDrawAccuracyEnabled(true);
        mMapView.getOverlays().add(myLocationOverlay);


        if(MODE!=MODE_DRAW){
            //on long press create new peppoint
            MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(getActivity(), new HapticMapEventLongPressListener() {
                @Override
                public void onLongPress(GeoPoint p) {
                    PepPoint pp = PepPoint.createEmpty(p);
                    mMapContent.pepPoints().put(pp.ID(), pp);
                }
            });
            //adds long press listener to mapoverlay
            mMapView.getOverlays().add(0, mapEventsOverlay);
        }else{
            //TODO toteuta DRAW
        }

                        /*Reactive implementation starts from here*/

        //if user is recording his movement will be drawn on every location change
        if(MODE == MODE_RECORD)
            startRecording();

        //animate to users current location as soon as possible
        myLocationOverlay.runOnFirstFix(() ->
                mMainSubscriptions.add(MainActivity.currentLocationObservable()
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .first()
                        .subscribe(loc ->
                                mapController.animateTo(new GeoPoint(loc.getLatitude(), loc.getLongitude()))))
        );

        mPepPointDatas = new ConcurrentHashMap<>();

        //PepPoint indicators are reactively, modified added and removed from the view
        mMainSubscriptions.add(mMapContent.pepPoints()
                .observable()
                .doOnNext(pepPointMap -> {
                    Observable.from(pepPointMap.keySet())
                            .mergeWith(Observable.from(mPepPointDatas.keySet()))
                            .forEach(id -> {
                                PepPoint pepPoint = pepPointMap.get(id);
                                if(!mPepPointDatas.containsKey(id) && pepPoint!=null) {
                                    //add Marker on PepPoint locations and Circles around marker
                                    Marker marker = createMarker(pepPoint);
                                    mMapView.getOverlays().add(marker);

                                    //Create circle
                                    Polygon circle = new Polygon(getActivity());
                                    circle.setStrokeWidth(2);
                                    circle.setStrokeColor(Color.BLUE);
                                    mMapView.getOverlays().add(circle);


                                    mPepPointDatas.put(id, new Trio<Marker, Polygon, Subscription>(marker, circle, Observable.combineLatest(
                                            pepPoint.triggerRadiusObservable(),
                                            pepPoint.locationObservable(), (radius, location) -> Pair.create(location, radius))
                                            .doOnNext(pair -> {
                                                //set marker and circle position according to peppoint position
                                                marker.setPosition(pepPoint.getLocation().asGeoPoint());
                                                //also set circle size according to radius
                                                circle.setPoints(Polygon.pointsAsCircle(pair.first.asGeoPoint(), pair.second));
                                            })
                                            .debounce(20, TimeUnit.MILLISECONDS)
                                            .subscribeOn(Schedulers.computation())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(v -> mMapView.invalidate())));
                                    //Create make a window that will pop up when peppoint is clicked
                                    PepMarkerInfoWindow window = new PepMarkerInfoWindow(getActivity(), R.layout.bubble_layout, mMapView, pepPoint, mMapContent.pepPoints());
                                    marker.setInfoWindow(window);

                                }else if(mPepPointDatas.containsKey(id) && pepPoint==null){//remove marker
                                    if(!pepPointMap.containsKey(id)){
                                        mMapView.getOverlays().remove(mPepPointDatas.get(id).first);
                                        mMapView.getOverlays().remove(mPepPointDatas.get(id).second);
                                        mPepPointDatas.get(id).third.unsubscribe();
                                    }
                                }
                            });
                }).debounce(50, TimeUnit.MILLISECONDS)
                .retryWhen(new RetryWithGrowingDelay(5, 100))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(v -> mMapView.invalidate()));

        //Init road width and color
        Polyline roadLine = new Polyline(getActivity());
        roadLine.setWidth(10);
        roadLine.setColor(MODE == MODE_WALK ? (Color.BLUE) : (Color.RED));

        //draw road on map. Changes on underlying route data is reactively changed in the UI
        mMainSubscriptions.add(mMapContent.route()
                .observable()
                .doOnNext(list -> {
                    //remove old road if any
                    mMapView.getOverlays().remove(roadLine);
                    //set new points to road. This also remove old roadpoints
                    roadLine.setPoints(Observable.from(list)
                            .map(item -> item.asGeoPoint())
                            .toList().toBlocking().single());
                    //add road back to mapoverlays
                    mMapView.getOverlays().add(roadLine);
                })
                .subscribeOn(Schedulers.computation())
                .subscribe(list -> {
                    mMapView.postInvalidate();
                },error ->  Log.i(TAG, "road error")));

        mMapView.invalidate();
        return view;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy mapview fragment");
        mMainSubscriptions.unsubscribe();
        Observable.from(mPepPointDatas.values())
                .forEach(trio -> trio.third.unsubscribe());
        mMapView.onDetach();
        super.onDestroy();
    }

    private Marker createMarker(PepPoint pepPoint){
        if(nodeIcon==null)
            nodeIcon = getResources().getDrawable(R.drawable.pep_point_location_indicator);
        Marker marker = new Marker(mMapView);
        marker.setIcon(nodeIcon);
        marker.setDraggable(true);
        //user can drag the marker by longpress and then drag
        marker.setOnMarkerDragListener(new MarkerDragListener(
                dragStart -> MainActivity.vibrator.vibrate(new long[]{50, 50, 50, 50}, -1),
                markerDrag -> pepPoint.setLocation(AdaptiveLocation.from(markerDrag.getPosition()))));
        return marker;
    }

    private void startRecording(){
        //TODO this is not tested
        RoadManager roadManager = new OSRMRoadManager(getActivity());
        mMainSubscriptions.add(MainActivity.currentLocationObservable()
                .filter(location -> location.getAccuracy() < REQUIRED_ACCURACY) //skip all too in accurate locationdatas
                .lift(SkipWhen.create((first, second) -> first.distanceTo(second) < 1)) //don't bother if points are less than 1 meter away from each other
                .map(location -> AdaptiveLocation.from(location))
                .lift(InPairs.create())
                .map(pair -> {
                    //put the elements in a list
                    ArrayList<GeoPoint> fromStartToEnd = new ArrayList<>(
                            Arrays.asList(pair.first.asGeoPoint(), pair.second.asGeoPoint())
                    );
                    //Get road from roadManager (this is usually done in a AsyncTask)
                    List<GeoPoint> roadManagersRoad = roadManager.getRoad(fromStartToEnd).mRouteHigh;
                    List<AdaptiveLocation> roadLine = new ArrayList<>(
                            Observable.from(roadManagersRoad)
                                    .map(geoPoint -> AdaptiveLocation.from(geoPoint))
                                    //if roadmanagers route deviates too much from the actual locations skip it
                                    .filter(aLoc -> aLoc.distanceFromLine(pair.first,pair.second)<=MAX_DEVIATION)
                                    .toList().toBlocking().single()
                    );
                    if(pair.second.distanceTo(roadLine.get(roadLine.size()-1))>MAX_DEVIATION){
                        roadLine.add(pair.second);
                    }
                    return roadLine;
                })
                .subscribeOn(Schedulers.io())
                .subscribe(list -> mMapContent.route().addAll(list)));
    }











    private void testRemoveAndAdd(){
        //TODO use this as test for adding an removing
        /*mMainSubscriptions.add(Observable.create(new Observable.OnSubscribe<GeoPoint>() {
            @Override
            public void call(Subscriber<? super GeoPoint> subscriber) {
                for(int i = 0; i<mMapContent.RXRoute().getList().size(); i++){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    subscriber.onNext(mMapContent.RXRoute().getList().get(i).asGeoPoint());
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.computation())
                .subscribe(next -> {
                    Log.i(TAG, next+"");
                    List<GeoPoint> points = roadLine.getPoints();
                    mMapView.getOverlays().removeFirst(roadLine);
                    points.add(next);//removeFirst
                    drawRoadOnMap(roadLine, points);
                }
        ));*/
    }
}

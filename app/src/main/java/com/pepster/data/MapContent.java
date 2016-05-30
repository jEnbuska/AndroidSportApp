package com.pepster.data;

import android.util.Log;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.firebase.client.DataSnapshot;
import com.firebase.client.utilities.PushIdGenerator;
import com.pepster.Interfaces.RunTimeUpdatable;
import com.pepster.Interfaces.UnSubscribePropagable;
import com.pepster.utilities.AdaptiveLocation;
import com.pepster.MainActivity;
import com.pepster.utilities.RetryWithGrowingDelay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;


/**
 * Created by WinNabuska on 12.3.2016.
 * MapContent main features are the Route and PepPoints (Optionally empty)
 * All the persistent values inside MapContent and inside its child objects must have proper
 * constructors (empty no parameters), setters and getters so that firebase know how to handle
 * them creating an instance of MapContent from a snashopt.
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class MapContent implements RunTimeUpdatable<MapContent>, UnSubscribePropagable {

    private static final String TAG = MapContent.class.getSimpleName();

    /**mID should not be change after it has ones been set*/
    private String mID;

    /**PepPoints should be mapped by their ID:s*/
    private ReactiveHashMap<String, PepPoint> mPepPoints;

    private ReactiveArrayList<AdaptiveLocation> mRoute;

    private BehaviorSubject<String> mTitleSubject;

    private Subscription mLocationChangeSubscription;

    /**modTime should be changed everytime changes are made and the object is send to DB*/
    /**lastUsage time variables is not in use*/
    private BehaviorSubject<Long> mLastUsageSubject, mModTimeSubject;

    public MapContent(){
        //mID = PushIdGenerator.generatePushChildName(System.nanoTime());
        mRoute = ReactiveArrayList.create();
        mPepPoints = new ReactiveHashMap();
        mTitleSubject = BehaviorSubject.<String>create();
        mLastUsageSubject = BehaviorSubject.<Long>create();
        mModTimeSubject = BehaviorSubject.<Long>create();
    }
    public void ID(String ID){mID=ID;}
    public String ID() {
        return mID;
    }

    /**
     * 'create(snapshot)' is very much prone to crashes if the snapshot contains any values
     * that do not have setters in MapContentClass.
     * @param snapshot is a Map object that is passed from Firebase by calling
     * 'ChildEventListeners' methods.
     * */
    public static MapContent create(DataSnapshot snapshot){
        MapContent content;
        //try {
        content = snapshot.getValue(MapContent.class);
        //Set data ID
        content.mID = snapshot.getKey();
        if(content.getTitle()==null)
            content.setTitle("");
        if(content.getRoute()==null)
            content.setRoute(new ArrayList<>());
        if(content.getPepPoints()==null)
            content.setPepPoints(new TreeMap<>());

        Log.i(TAG, content + "");
        //for each peppoint set ID and newly created content as parent
        content.mPepPoints.observable()
                .subscribeOn(Schedulers.computation())
                .doOnNext(map -> {
                    Observable.from(map.entrySet())
                            .forEach(//for every peppoint set the parent as parent own ID
                                    entry -> {
                                        PepPoint pp = entry.getValue();
                                        pp.initialize(entry.getKey());
                                    },
                                    error->Log.i(TAG, error.getMessage())
                            );
                }).retryWhen(new RetryWithGrowingDelay(5, 70))
                .subscribe();
        //}catch (Exception e){Log.i("CREATE FAIL", e.getMessage()); return null;}
        return content;
    }

    /**returns a observable that will send a String message about what has chaged
     * everytime something is changed.
     * Make sure that you unsubscibe from this Observable before calling updateWith(mostRecentVersion)
     * Also see method updateWith(...)
     * @return Observable<String>
     */
    public Observable<String> updateObservable(){
        Observable<String> observable = titleObservable().skip(1).map(v -> "title changed")
                .mergeWith(route().observable().skip(1).map(v -> "route changed"))
                .mergeWith(pepPoints().observable().skip(1).map(v -> "peppoint map changed"));
        for (PepPoint pepPoint : mPepPoints.getMap().values())
            observable = observable.mergeWith(pepPoint.updateObservable());
        return observable;
    }

    /**Calling updateWith(other) will make this object updateWith it self with the other objects
     * data. Values are changed using 'setters' so everyone that is subscribed will immediately
     * receive the updated values
     * @param mostRecentVersion When you call this method makes sure that the object you pass has
     * the same ID and a later modTime.
     * @exception @NullPointerException. Due to multihreading issues sometimes all the items have not
     * been full initialized when this method is called. That is way methods  'retryWhen'
     * need to exist.
     * also see method 'updateObservable()'. Make sure that you have called unsubscribe on the object that
     * updateObservable() method returns before calling this.
     * */
    public void updateWith(MapContent mostRecentVersion){
        Observable.just(mostRecentVersion)
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnNext(fresh -> {
                    setTitle(fresh.getTitle());
                    setLastUsage(fresh.getLastUsage());
                    setModTime(fresh.getModTime());
                    if (!mRoute.equals(fresh.mRoute))
                        setRoute(fresh.mRoute.getList());
                    if (!mPepPoints.equals(fresh.mPepPoints)) {
                        Observable.from(mPepPoints.leftOuterJoin(fresh.mPepPoints))
                                .forEach(id -> mPepPoints.remove(id));
                        Observable.from(mPepPoints.rightOuterJoin(fresh.mPepPoints))
                                .forEach(id -> {
                                    PepPoint newPepPoint = fresh.mPepPoints.get(id);
                                    mPepPoints.put(id, newPepPoint);
                                    newPepPoint.initialize(id);
                                }, e -> Log.e(TAG, e.toString()));
                        Observable.from(mPepPoints.innerJoin(fresh.mPepPoints))
                                .filter(id -> !mPepPoints.get(id).equals(fresh.mPepPoints.get(id)))
                                .doOnNext(id -> {
                                    PepPoint updated = fresh.mPepPoints.get(id);
                                    mPepPoints.get(id).updateWith(updated);
                                    updated.initialize(id);
                                }).retryWhen(new RetryWithGrowingDelay(4, 20))
                                .subscribeOn(AndroidSchedulers.mainThread())
                                .subscribe();                    }
                }).retryWhen(new RetryWithGrowingDelay(5, 30))
                .subscribe();
    }

    /**SETTERS
     * All the setters should be called from the main thread
     * setRoute and setPepPoints can be called from any thread*/
    public void setModTime(long modTime) {
        mModTimeSubject.onNext(modTime);
    }
    public void setPepPoints(Map<String, PepPoint> pepPoints) {
        mPepPoints.clearAndPutAll(pepPoints);
    }
    public void setTitle(String title) {
        mTitleSubject.onNext(title);
    }
    public void setLastUsage(long lastUsage) {
        mLastUsageSubject.onNext(lastUsage);
    }

    public void setRoute(List<AdaptiveLocation> route){
        mRoute.clearAndAddAll(route);
    }

    public void unSubscribeLocationChangeUpdates(){
        mLocationChangeSubscription.unsubscribe();
    }


    /**
     * Calling subscribeToLocationChangeUpdates
     */
    public void subscribeToLocationChangeUpdates(){
        mLocationChangeSubscription = MainActivity.currentLocationObservable()
                .distinctUntilChanged()
                .debounce(1, TimeUnit.SECONDS)
                .filter(location -> location.getAccuracy()<30)
                .subscribeOn(Schedulers.computation())
                .subscribe(currentLocation -> {
                    mPepPoints.observable()
                            .flatMap(map -> Observable.from(map.values()))
                            .first(pepPoint -> pepPoint.shouldFireNow(currentLocation, mPepPoints.getMap()))
                            .observeOn(AndroidSchedulers.mainThread())
                            .forEach(pepPoint -> {
                                        PepPoint.playMessage(pepPoint);
                                        pepPoint.isActive(false);
                                    },
                                    error -> Log.i(TAG, "no peppoint near")
                            );
                });
    }

    /**create empty can be used when recording new MapContent*/
    public static MapContent createEmpty(){
        MapContent emptyContent = new MapContent();
        emptyContent.mID=PushIdGenerator.generatePushChildName(System.nanoTime());
        emptyContent.setPepPoints(new HashMap<>());
        emptyContent.setTitle(new SimpleDateFormat("dd/M/yyyy").format(new Date()));
        emptyContent.setLastUsage(System.currentTimeMillis());
        emptyContent.setModTime(System.currentTimeMillis());
        return emptyContent;
    }


    /**Avoid using getters when ever possible.
     * Instead try to use observable values*/
    public long getModTime() {
        return mModTimeSubject.getValue();
    }
    public List<AdaptiveLocation> getRoute(){
        return route().getList();
    }
    public String getTitle() {
        return mTitleSubject.getValue();
    }
    public Map<String, PepPoint> getPepPoints() {
        return mPepPoints.getMap();
    }
    public long getLastUsage() {
        return mLastUsageSubject.getValue();
    }

    public ReactiveArrayList<AdaptiveLocation> route(){
        return mRoute;
    }
    public ReactiveHashMap<String, PepPoint> pepPoints(){
        return mPepPoints;
    }

    public Observable<String> titleObservable(){
        return mTitleSubject.asObservable();
    }

    /**Read the documentation of Interface UnSubscribePropagable*/
    @Override
    public void propagateUnSubscribe() {
        mLastUsageSubject.onCompleted();
        mLocationChangeSubscription.unsubscribe();
        mTitleSubject.onCompleted();
        mModTimeSubject.onCompleted();
    }

    /**Sets all PepPoints active so when the MapContent is opened again the PepPoints will trigger
     * their message when the user enters them for the first time*/
    public void resetToDefault() {
        mPepPoints.observable()
                .flatMap(map ->  Observable.from(map.values()))
                .forEach(pepPoint ->  pepPoint.isActive(true));
    }


    @Override
    public boolean equals(Object object) {
        if(object instanceof MapContent){
            MapContent other = (MapContent) object;
            return other.mID.equals(mID) && other.getModTime()==getModTime() && mPepPoints.equals(other.mPepPoints);
        }else
            return false;
    }

    @Override
    public String toString() {
        String pepPointString = Stream.of(mPepPoints.getMap().values()).map(pp -> pp.toString()).collect(Collectors.joining(", "));
        String routeString = Stream.of(mRoute.getList()).map(loc -> loc.getLat() + " : " + loc.getLng()).collect(Collectors.joining(", "));
        return "ID: " + mID + ", " + getTitle() + ", mod: " + getModTime() + ", usage: " + getLastUsage() + "\nPepPoints: "  +pepPointString + "\nRoute: " + routeString;
    }


    public static MapContent createTestObject(){
        MapContent emptyContent = new MapContent();
        emptyContent.setPepPoints(new HashMap<>());
        emptyContent.setTitle(new SimpleDateFormat("dd/M/yyyy").format(new Date()));
        emptyContent.setLastUsage(System.currentTimeMillis());
        emptyContent.setModTime(System.currentTimeMillis());
        emptyContent.mRoute = ReactiveArrayList.create();
        emptyContent.mPepPoints = new ReactiveHashMap<>();
        emptyContent.mTitleSubject = BehaviorSubject.<String>create();
        emptyContent.mLastUsageSubject = BehaviorSubject.<Long>create();
        emptyContent.mModTimeSubject = BehaviorSubject.<Long>create();
        return emptyContent;
    }

    public static MapContent createTestObject2(){
        return new MapContent();
    }

    /*public AdaptiveLocation calculateCenter() {
        double latMin = 180, latMax = -180, lonMin = 180, lonMax = -180;
        for (AdaptiveLocation point : route().getList()) {
            double lat = point.getLat(), lon = point.getLng();
            if (latMin > lat) {
                latMin = lat;
            } else if (latMax < lat) {
                latMax = lat;
            }
            if (lonMin > lon) {
                lonMin = lon;
            } else if (lonMax < lon) {
                lonMax = lon;
            }
        }
        return new AdaptiveLocation((latMax + latMin) / 2, (lonMax + lonMin) / 2);
    }*/
}

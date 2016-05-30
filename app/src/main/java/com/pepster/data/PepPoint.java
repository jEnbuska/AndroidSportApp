package com.pepster.data;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.firebase.client.utilities.PushIdGenerator;
import com.pepster.Interfaces.RunTimeUpdatable;
import com.pepster.Interfaces.UnSubscribePropagable;
import com.pepster.utilities.AdaptiveLocation;
import com.pepster.MainActivity;
import com.pepster.R;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import rx.Observable;
import rx.subjects.BehaviorSubject;


/**
 * Created by WinNabuska on 12.3.2016.
 *
 *
 * */
@JsonIgnoreProperties({"active", "ID"})
public class PepPoint implements RunTimeUpdatable<PepPoint>, UnSubscribePropagable {

    private static Context context;
    private static Map<String, TextToSpeech> textToSpeeches;
    private static final String TAG = PepPoint.class.getSimpleName();
    public static final String TYPE_DIRECTION = "type direction", TYPE_CUSTOM = "type custom", TYPE_MUSIC = "type music";

    public String mID;
    private BehaviorSubject<AdaptiveLocation> mLocationSubject;
    private BehaviorSubject<Integer> mTriggerRadiusSubject;
    private BehaviorSubject<Boolean> mIsActiveSubject;
    private BehaviorSubject<String> mMessageSubject, mTypeSubject, mLanguageSubject;
    /***precondition subject is currently not in use*/
    private BehaviorSubject<List<String>> mPreconditionsSubject;

    public PepPoint(){
        initSubjects();
    }
    public void ID(String ID){
        mID=ID;
    }
    public String ID(){
        return mID;
    }

    /**Call init context always before you create any PepPoints
     * Context needs to be initialized so that TextToSpeeches can be created and standard parameters
     * from resources can be fetched*/
    public static void initContext(Context context){
        PepPoint.context=context;
        textToSpeeches=new HashMap<>();
    }

    /**initializes the TextToSpeech in the Locale string parameter passed
     * List acceptable locale Strings:
     * [ar, ar_EG, bg, bg_BG, ca, ca_ES, cs, cs_CZ, da, da_DK, de, de_AT, de_BE, de_CH, de_DE, de_LI, de_LU, el, el_CY, el_GR, en, en
     _AU, en_BE, en_BW, en_BZ, en_CA, en_GB, en_HK, en_IE, en_IN, en_JM, en_MH, en_MT, en_NA, en_NZ, en_PH, en_PK, en_RH, en_SG, en_TT, en_US, en_US_POSIX,
     en_VI, en_ZA, en_ZW, es, es_AR, es_BO, es_CL, es_CO, es_CR, es_DO, es_EC, es_ES, es_GT, es_HN, es_MX, es_NI, es_PA, es_PE, es_PR, es_PY, es_SV, es_US
     , es_UY, es_VE, et, et_EE, eu, eu_ES, fa, fa_IR, fi, fi_FI, fr, fr_BE, fr_CA, fr_CH, fr_FR, fr_LU, fr_MC, gl, gl_ES, hr, hr_HR, hu, hu_HU, in, in_ID,
     is, is_IS, it, it_CH, it_IT, iw, iw_IL, ja, ja_JP, kk, kk_KZ, ko, ko_KR, lt, lt_LT, lv, lv_LV, mk, mk_MK, ms, ms_BN, ms_MY, nl, nl_BE, nl_NL, no, no_N
     O, no_NO_NY, pl, pl_PL, pt, pt_BR, pt_PT, ro, ro_RO, ru, ru_RU, ru_UA, sh, sh_BA, sh_CS, sh_YU, sk, sk_SK, sl, sl_SI, sq, sq_AL, sr, sr_BA, sr_ME, sr_
     RS, sv, sv_FI, sv_SE, th, th_TH, tr, tr_TR, uk, uk_UA, vi, vi_VN, zh, zh_CN, zh_HK, zh_HANS_SG, zh_HANT_MO, zh_MO, zh_TW]*/
    public static synchronized void createTTSIfMissing(String language){
        if(!textToSpeeches.containsKey(language)){
            textToSpeeches.put(language, (new TextToSpeech(context, onInitListenerStatus -> {
                if (onInitListenerStatus != TextToSpeech.ERROR) {
                    String[] localeParams = language.split("_");
                    Log.i(TAG, "text to speech initialized " + localeParams[0]);
                    textToSpeeches.get(language).setLanguage(new Locale(localeParams[0], localeParams[1]));
                } else {
                    throw new Error("onInitListenerStatus was ERROR");
                }
            })));
        }
    }

    /**speaks the peppoints message in then language defined by peppoints language parameter
     * before calling this create text to speech with the peppoints language must be called
     * */
    public static synchronized void playMessage(PepPoint pepPoint) {
        if(Build.VERSION.SDK_INT < 21){
            textToSpeeches.get(pepPoint.getLanguage()).speak(pepPoint.getMessage(), TextToSpeech.QUEUE_ADD, null);
        }else{
            textToSpeeches.get(pepPoint.getLanguage()).speak(pepPoint.getMessage(), TextToSpeech.QUEUE_ADD, null, "" + pepPoint.getMessage());
        }
        Log.i(TAG, "spoke in " + pepPoint.getLanguage());
    }

    /**
     * Creates an empty that should have all the appropriate values initialized
     * @param location
     * @return PepPoint*/
    public static PepPoint createEmpty(GeoPoint location){
        PepPoint pp = new PepPoint();
        pp.initialize(PushIdGenerator.generatePushChildName(System.nanoTime()));
        pp.setLocation(new AdaptiveLocation(location.getLatitude(), location.getLongitude()));
        pp.setMessage(context.getString(R.string.empty_message));
        pp.setTriggerRadius(context.getResources().getInteger(R.integer.radius_default));
        pp.setType(PepPoint.TYPE_CUSTOM);
        pp.setPreconditions(new ArrayList<>());
        pp.setLanguage(context.getResources().getConfiguration().locale.toString());
        return pp;
    }


    /**sets isActive state to true and. I
     * nitializes all subjects so that setting and getting values becomes possible
     * */
    private void initSubjects(){
        mMessageSubject = BehaviorSubject.<String>create();
        mTriggerRadiusSubject = BehaviorSubject.<Integer>create();
        mLocationSubject = BehaviorSubject.<AdaptiveLocation>create();
        mLanguageSubject = BehaviorSubject.<String>create();
        mTypeSubject = BehaviorSubject.<String>create();
        mIsActiveSubject = BehaviorSubject.<Boolean>create();
        isActive(true);
        mPreconditionsSubject = BehaviorSubject.<List<String>>create();
        setPreconditions(Arrays.asList());
    }

    /**read interface 'RunTimeUpdatable' documentation*/
    @Override
    public void updateWith(PepPoint newPepPoint){
        try {
            if (!getType().equals(newPepPoint.getType()))
                setType(newPepPoint.getType());
            if (!getMessage().equals(newPepPoint.getMessage()))
                setMessage(newPepPoint.getType());
            if (!getLanguage().equals(newPepPoint.getLanguage()))
                setLanguage(newPepPoint.getLanguage());
            if (!getLocation().equals(newPepPoint.getLocation()))
                setLocation(newPepPoint.getLocation());
            if (Math.abs(getTriggerRadius() - newPepPoint.getTriggerRadius()) > (0.0000089 / 2/*half a meter*/))
                setTriggerRadius(newPepPoint.getTriggerRadius());
            if (!getPreconditions().equals(newPepPoint.getPreconditions()))
                setPreconditions(newPepPoint.getPreconditions());
        }catch (NullPointerException e){
            Log.e(MainActivity.APP, "unPlanned NullPointerException in 'PepPoint - copyUpdatesFrom'");
        }
    }

    /**
     * After update observable start emitting update information after listed values are changed for
     * the second time. The change first update happens during initialization and the data about
     * that initialization should not be considered as update
     * @return Observable String message about what has changed
     */
    public Observable<String> updateObservable(){
        return
                messageObservable().distinctUntilChanged().skip(1)
                        .map(v -> "peppoint "+ ID()+" message changed")
                        .mergeWith(typeObservable().distinctUntilChanged().skip(1)
                                .map(v -> "peppoint "+mID+" type changed"))
                        .mergeWith(locationObservable().distinctUntilChanged().skip(1)
                                .map(v -> "peppoint "+mID+" location changed "))
                        .mergeWith(preconditionsObservable().distinctUntilChanged().skip(1)
                                .map(v -> "peppoint "+mID+" preconditions changed"))
                        .mergeWith(languageObservable().distinctUntilChanged().skip(1)
                                .map(v -> "peppoint "+mID+" language changed"))
                        .mergeWith(triggerRadiusObservable().distinctUntilChanged().skip(1)
                                .map(v -> "peppoint "+mID+" radius changed"));
    }

    /**
     *
     * After creating MapContent, always call initialize for every MapContents PepPoints.
     * initialize checks that all the values have been set at least ones, so that when any
     * parameters change for the first time, the changes are propagated accordingly to everyone
     * who is subscribed to the Observable created by method 'updateObservable()'
     * @param ID
     */
    public void initialize(String ID){
        ID(ID);
        if(getPreconditions()==null)
            setPreconditions(new ArrayList<>());
        if(getLanguage()==null)
            setLanguage("en_US");
        if(getPreconditions()==null)
            setPreconditions(new ArrayList<>());
        if(getLocation() == null)
            setLocation(new AdaptiveLocation(0,0));
        if(getMessage()==null)
            setMessage("");
        if(getTriggerRadius()==-1)
            setTriggerRadius(17);
        if(getType()==null)
            setType(PepPoint.TYPE_CUSTOM);
    }


    /**
     * Returs true if PepPoint has not fired yet (and assuming user has not turned it manually off
     * or back on) and if users current location is within PepPoints range (that is determined by
     * the triggerRadius)
     * @param currentLocation
     * @param otherPepPoints
     * @return boolean
     */
    public boolean shouldFireNow(Location currentLocation, Map<String, PepPoint> otherPepPoints){
        Log.i(TAG, "distance " + getLocation().distanceTo(currentLocation));
        if(mIsActiveSubject.getValue() && getLocation().distanceTo(currentLocation) <= getTriggerRadius()) {
            return true;/*mPreconditionsSubject
                    .map(preC -> otherPepPoints.get(preC))
                    .all(pepPoint -> pepPoint == null || !pepPoint.isActive())
                    .defaultIfEmpty(true)
                    .toSingle()
                    .toBlocking()
                    .value();*/
        }else{
            return false;
        }
    }


    /**OBSERVABLES*/

    public Observable<String> languageObservable(){
        return mLanguageSubject.asObservable();
    }
    public Observable<Integer> triggerRadiusObservable(){
        return mTriggerRadiusSubject.asObservable();
    }
    public Observable<String> messageObservable(){
        return mMessageSubject.asObservable();
    }/**preconditions are currently not in use*/
    public Observable<List<String>> preconditionsObservable(){
        return mPreconditionsSubject.asObservable();
    }
    public Observable<AdaptiveLocation> locationObservable(){
        return mLocationSubject.asObservable();
    }
    public Observable<Boolean> isActiveObservable(){
        return mIsActiveSubject.asObservable();
    }
    public Observable<String> typeObservable(){
        return mTypeSubject.asObservable();
    }


    /**SETTERS
     * All the setters should be called from the main thread
     */
    public void isActive(boolean active) {
        mIsActiveSubject.onNext(active);
    }
    public void setLanguage(String language) {
        createTTSIfMissing(language);
        mLanguageSubject.onNext(language);
    }
    public void setLocation(AdaptiveLocation location) {
        mLocationSubject.onNext(location);
    }
    public void setMessage(String message) {
        mMessageSubject.onNext(message);
    }
    public void setTriggerRadius(int triggerRadius) {
        mTriggerRadiusSubject.onNext(triggerRadius);
    }

    public void setType(String type) {
        mTypeSubject.onNext(type);
    }

    public void setPreconditions(List<String> preconditions) {
        mPreconditionsSubject.onNext(preconditions);
    }

    /** GETTERS
     * Avoid using getters when ever possible. Instead use observables.
     */
    public boolean isActive() {
        return mIsActiveSubject.getValue();
    }
    public List<String> getPreconditions() {
        return mPreconditionsSubject.getValue();
    }
    public String getLanguage() {
        return mLanguageSubject.getValue();
    }
    public AdaptiveLocation getLocation() {
        return mLocationSubject.getValue();
    }
    public String getMessage() {
        return mMessageSubject.getValue();
    }/** getTriggerRadius returns -1 upon null pointer exception.*/
    public int getTriggerRadius() {
        try {
            return mTriggerRadiusSubject.getValue();
        }catch (NullPointerException e){return -1;}
    }/**Doesn't have any functionality. Maybe later there could be types that would play a song's
     * from Spotify or give more standard navigation instructions.*/
    public String getType() {
        return mTypeSubject.getValue();
    }

    public static void shutDownTTS(){
        if(textToSpeeches!=null) {
            for (TextToSpeech tts : textToSpeeches.values()) {
                tts.stop();
                tts.shutdown();
            }
            textToSpeeches.clear();
        }
    }

    /**
     * implementation of UnSubscribePropagable interface
     */
    @Override
    public void propagateUnSubscribe() {
        mTypeSubject.onCompleted();
        mLanguageSubject.onCompleted();
        mMessageSubject.onCompleted();
        mIsActiveSubject.onCompleted();
        mTypeSubject.onCompleted();
        mTriggerRadiusSubject.onCompleted();

        mPreconditionsSubject.onCompleted();
    }

    @Override
    public String toString() {
        return "message: " + getMessage() + ", trigger radius: " + getTriggerRadius() + ", active: "
                + isActive() + ", language: " + getLanguage() + ", preconditions, " +
                Arrays.toString(getPreconditions().toArray()) + ", type: " + getType() +
                ", location: " + getLocation().getLat() + ":" + getLocation().getLng();
    }

    /**
     * Checks if the object is a shallow copy of this object
     * @param otherObject
     * @return
     */
    @Override
    public boolean equals(Object otherObject) {
        if(otherObject instanceof PepPoint){
            PepPoint other = (PepPoint) otherObject;
            return other.mID.equals(mID) &&
                    getMessage().equals(other.getMessage()) &&
                    getLocation().equals(other.getLocation()) &&
                    getType().equals(other.getType()) &&
                    getLanguage().equals(other.getLanguage()) &&
                    Math.abs(getTriggerRadius()-other.getTriggerRadius()) < 1 &&
                    getPreconditions().equals(other.getPreconditions());
        }else{
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (mID.hashCode() + getMessage().hashCode() + getLocation().hashCode() +
                getLanguage().hashCode() + getType().hashCode() +
                getPreconditions().hashCode()) % Integer.MAX_VALUE;
    }

    //this constructor is only for debugging purposes
    public PepPoint(AdaptiveLocation location, String message, int triggerRadiusMeters, String type){
        initSubjects();
        setLocation(location);
        setMessage(message);
        setTriggerRadius(triggerRadiusMeters);
        setType(type);
        setPreconditions(new ArrayList<>());
        setLanguage(context.getResources().getConfiguration().locale.toString());
    }

}

package com.pepster.views;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.bonuspack.overlays.MarkerInfoWindow;
import org.osmdroid.views.MapView;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import android.util.Pair;

import com.pepster.utilities.AdaptiveLocation;
import com.pepster.MainActivity;
import com.pepster.data.ReactiveHashMap;
import com.pepster.R;
import com.pepster.data.PepPoint;
import com.pepster.Interfaces.AfterTextChangeListener;
import com.pepster.Interfaces.SeekBarProgressChangeListener;

/**
 * Created by WinNabuska on 12.3.2016.
 */
public class PepMarkerInfoWindow extends MarkerInfoWindow {

    private static final String TAG = PepMarkerInfoWindow.class.getSimpleName();
    private CompositeSubscription mSubscriptions;
    private NumberFormat doubleFormat;
    private TextView mTitleTV;
    private DiscreteSeekBar mRadiusSeekBar;
    private TextView mRadiusTV;
    private EditText mMessageED;
    private Switch mActiveSwitch;
    private ImageButton mBinButton;
    private PepPoint mPepPoint;
    private Context mContext;
    private AppCompatSpinner mLanguageSpinner;
    private AppCompatSpinner mTypeSpinner;
    private String [] mTypes, mLanguages;

    /**PepMarkerInfoWindow uses PepPoint map for deleting it self from the mapContent if the user
     * choices to do so*/
    public PepMarkerInfoWindow(Context context, int layoutResId, MapView mapView,
                               final PepPoint pepPoint, final ReactiveHashMap<String, PepPoint> pepPoints) {
        super(layoutResId, mapView);
        mTypes = context.getResources().getStringArray(R.array.peppoint_types);
        mLanguages = context.getResources().getStringArray(R.array.peppoint_languages);
        mPepPoint=pepPoint;
        mContext=context;
        doubleFormat = new DecimalFormat("#####.#");
        mTitleTV = (TextView) mView.findViewById(R.id.bubble_title);
        mRadiusSeekBar = (DiscreteSeekBar) mView.findViewById(R.id.radius_seekbar);
        mRadiusTV = (TextView) mView.findViewById(R.id.pep_point_radius_tv);
        mMessageED = ((EditText) mView.findViewById(R.id.message_ed));
        mActiveSwitch = (Switch) mView.findViewById(R.id.peppoint_switch);
        mBinButton = (ImageButton) mView.findViewById(R.id.bin_ib);
        mLanguageSpinner =(AppCompatSpinner) mView.findViewById(R.id.peppoint_language_spinner);
        mTypeSpinner = (AppCompatSpinner) mView.findViewById(R.id.peppoint_type_spinner);

        mView.findViewById(R.id.play_peptalk_message_btn).setOnClickListener(v -> PepPoint.playMessage(pepPoint));

        mMessageED.addTextChangedListener(new AfterTextChangeListener() {
            @Override
            public void afterTextChanged(Editable text) {
                pepPoint.setMessage(text.toString());
            }
        });

        mRadiusSeekBar.setOnProgressChangeListener(new SeekBarProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                if(fromUser)
                    pepPoint.setTriggerRadius(value);
            }
        });

        mActiveSwitch.setOnCheckedChangeListener((btn, isChecked) -> pepPoint.isActive(isChecked));

        mBinButton.setOnClickListener(v -> {
            pepPoints.remove(pepPoint.ID());
            close();
        });

        mLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pepPoint.setLanguage(mLanguages[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pepPoint.setType(mTypes[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    public void onOpen(Object item) {
        for (InfoWindow window : getOpenedInfoWindowsOn(mMapView))
            window.close();

        mSubscriptions = new CompositeSubscription();

        String metersString = mContext.getString(R.string.meters), radiusString = mContext.getString(R.string.radius_text), distanceString = mContext.getString(R.string.distance);

        mSubscriptions.add(Observable.combineLatest(
                mPepPoint.locationObservable(),
                MainActivity.currentLocationObservable(),
                (pointLocation, lastLocation) -> new Pair(pointLocation, lastLocation))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    String roundedDistance = doubleFormat.format(((AdaptiveLocation) pair.first).asLocation().distanceTo((Location) pair.second));
                    mTitleTV.setText(distanceString + " " + roundedDistance + " " + metersString);
                }));

        mSubscriptions.add(mPepPoint.triggerRadiusObservable()
                .distinctUntilChanged()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(radius -> mRadiusTV.setText(radiusString + " " + radius + " " + metersString)));

        mSubscriptions.add(mPepPoint.messageObservable()
                .distinctUntilChanged()
                .filter(v -> !mMessageED.hasFocus())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(message -> mMessageED.setText(message), error -> Log.e(TAG, "error in messageObservable")));

        mSubscriptions.add(mPepPoint.triggerRadiusObservable()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(radius -> mRadiusSeekBar.setProgress(radius)));

        mSubscriptions.add(mPepPoint.isActiveObservable()
                .filter(active -> active.booleanValue() != mActiveSwitch.isChecked())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(checked -> mActiveSwitch.setChecked(checked)));

        mSubscriptions.add(mPepPoint.languageObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(language -> {
                    if(language.equals(mLanguages[0])){
                        mLanguageSpinner.setSelection(0);
                    }else if(language.equals(mLanguages[1])){
                        mLanguageSpinner.setSelection(1);
                    }else if(language.equals(mLanguages[2])){
                        mLanguageSpinner.setSelection(2);
                    }else{
                        throw new Error();
                    }
                }));

        mSubscriptions.add(mPepPoint.typeObservable()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(type -> {
                    if(type.equals(mTypes[0])){
                        mTypeSpinner.setSelection(0);
                    }else if(type.equals(mTypes[1])){
                        mTypeSpinner.setSelection(1);
                    }else if(type.equals(mTypes[2])){
                        mTypeSpinner.setSelection(2);
                    }else{//redundant
                        mTypeSpinner.setSelection(0);
                    }
                }));
    }

    @Override
    public void onClose() {
        mSubscriptions.unsubscribe();
        mSubscriptions=null;
        super.onClose();
    }
}

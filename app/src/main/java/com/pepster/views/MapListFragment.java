package com.pepster.views;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.google.android.gms.maps.MapView;
import com.pepster.MapRecyclerViewAdapter;
import com.pepster.R;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapListFragment extends Fragment {

    private static final String TAG = MapListFragment.class.getSimpleName();

    private SpinnerSelectionListener mSpinnerSelectionListener;
    private MapRecyclerViewAdapter mRecyclerViewAdapter;

    public static MapListFragment newInstance(MapRecyclerViewAdapter recyclerViewAdapter, SpinnerSelectionListener spinnerSelectionListener) {
        MapListFragment fragment = new MapListFragment();
        fragment.mSpinnerSelectionListener = spinnerSelectionListener;
        fragment.mRecyclerViewAdapter = recyclerViewAdapter;
        //fragment.setRetainInstance(false);
        return fragment;
    }

    public MapListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView listviewfragment");
        View view = inflater.inflate(R.layout.list_fragment_layout, container, false);

        //TODO implement spinner that sorts the MapContent by whatever
        /*Spinner spinner = (Spinner)view.findViewById(R.id.sortby_spinner);
        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.comparators, android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(mSpinnerSelectionListener);*/
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.map_recycler_view);
        // Determine the number of columns to display, based on screen width.
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        //Wrap the listadapter in animation adapters
        ScaleInAnimationAdapter scaleAnimAdapter = new ScaleInAnimationAdapter(mRecyclerViewAdapter);
        scaleAnimAdapter.setInterpolator(new OvershootInterpolator(0));
        scaleAnimAdapter.setDuration(550);
        scaleAnimAdapter.setFirstOnly(false);
        recyclerView.setAdapter(mRecyclerViewAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume setAdapter");
        if (mRecyclerViewAdapter != null) {
            for (MapView m : mRecyclerViewAdapter.getMapViews()) {
                m.onResume();//Paska vuotaa muistia
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRecyclerViewAdapter != null) {
            for (MapView m : mRecyclerViewAdapter.getMapViews()) {
                m.onDestroy();
            }
        }
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        if (mRecyclerViewAdapter != null) {
            for (MapView m : mRecyclerViewAdapter.getMapViews()) {
                m.onPause();//Paska vuotaa muistia
            }
        }
    }

    @Override
    public void onLowMemory() {
        Log.i(TAG, "onLowMemory");
        super.onLowMemory();
        if (mRecyclerViewAdapter != null) {
            for (MapView m : mRecyclerViewAdapter.getMapViews()) {
                m.onLowMemory();
            }
        }
    }

    public abstract static class SpinnerSelectionListener implements AdapterView.OnItemSelectedListener{
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }
}

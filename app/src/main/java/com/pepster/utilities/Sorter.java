package com.pepster.utilities;

import java.util.Comparator;
import java.util.Map;

/**
 * Created by WinNabuska on 12.3.2016.
 */
public class Sorter<R> {

    private Map<String, Comparator<R>> mComparators;
    private String mState;

    public Sorter(Map<String, Comparator<R>> comparators, String defState){
        mComparators=comparators;
        mState=defState;
    }

    public Comparator<R> getComparator(){
        return mComparators.get(mState);
    }

    public void setState(String state){
        mState=state;
    }
}

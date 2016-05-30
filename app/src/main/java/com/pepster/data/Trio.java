package com.pepster.data;

import android.support.v4.util.Pair;

/**
 * Created by WinNabuska on 23.4.2016.
 * a simple data carrier object
 */
public class Trio<F,S,T> extends Pair<F,S> {

    public T third;

    public Trio(F first, S second, T third) {
        super(first,second);
        this.third = third;
    }

    public <F,S,T>Trio<F,S,T>create(F first, S second, T third) {
        return new Trio<>(first,second,third);
    }
}

package com.pepster.Interfaces;

/**
 * Created by WinNabuska on 23.4.2016.
 *
 * This interface works well hand in hand with RXJavas reactive framework.
 * UnSubscribePropagable is an object that upon 'propagateUnSubscribe' call, calls onComplete on
 * every Subject and unSubscribes from all Observables its subscibet to. Also it calls
 * 'propagateUnSubscribe to all it object references that are instances of
 * UnSubscribePropagable
 *
 * Implementors: MapContent, PepPoint, ReactiveHashMap, ReactiveArrayList
 */

public interface UnSubscribePropagable {
    /**upon call should the object call onComplete on all its Observable Subjects and also
     * un subscribe all subscriptions that might be open.
     * Also if the objects has any child objects that implement the same interface
     * it should call propagateUnSubscribe for each one of them*/
    void propagateUnSubscribe();
}

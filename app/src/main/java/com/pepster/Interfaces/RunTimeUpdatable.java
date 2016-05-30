package com.pepster.Interfaces;

import rx.Observable;

/**
 * Created by WinNabuska on 22.4.2016.
 * This interface works well hand in hand with RXJavas reactive framework.
 * RunTimeUpdatable is a object that is able to merge all updates from an updated object.
 * The benefit of that is that the object does not need to be replaced when a more recent version
 * of the object is received (for example from firebase db). The a minimum amount of
 * subscriptions need to cut of and initialized, when the object is updated at the runtime
 */
public interface RunTimeUpdatable<R> {

    /**
     * after updateWith has been called should this object look like a deep copy clone of
     * freshValue object.
     * It is very important that if you are about to call call this method you first un subscribe
     * any subscriptions that would could resend the information about the change to the same source
     * that initially sent the updated object to this client
     * @param freshValue
     */
    void updateWith(R freshValue);

    /**
     * updateObservable should emit messages about what has changed in this object.
     * updateObject should emit a message every time a setSomeVariable(someParam) call
     * has happened
     * When you are about to call call 'updateWith(...)', to avoid needless recursive work between
     * server and this client, un subscribe any possible subscriptions that would resend the
     * information about the change to the same source that initially send the updated object to
     * this client
     */
    Observable<String> updateObservable();

}

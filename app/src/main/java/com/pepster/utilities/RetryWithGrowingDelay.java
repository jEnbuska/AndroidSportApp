package com.pepster.utilities;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;


public class RetryWithGrowingDelay implements Func1<Observable<? extends Throwable>, Observable<?>> {

    private final static String TAG = RetryWithGrowingDelay.class.getSimpleName();
    private final int _maxRetries;
    private final int _retryDelayMillis;
    private int _retryCount;

    public RetryWithGrowingDelay(final int maxRetries, final int retryDelayMillis) {
        _maxRetries = maxRetries;
        _retryDelayMillis = retryDelayMillis;
        _retryCount = 0;
    }

    // this is a notificationhandler, all that is cared about here
    // is the emission "type" not emission "content"
    // only onNext triggers a re-subscription (onError + onComplete kills it)

    @Override
    public Observable<?> call(Observable<? extends Throwable> inputObservable) {
        // it is critical to use inputObservable in the chain for the result
        // ignoring it and doing your own thing will break the sequence
        return inputObservable.flatMap(new Func1<Throwable, Observable<?>>() {
            @Override
            public Observable<?> call(Throwable throwable) {
                Log.i(TAG, "retry call");
                if (++_retryCount < _maxRetries) {
                    Log.i(TAG, "retry when start");
                    return Observable.timer(_retryCount * _retryDelayMillis,
                            TimeUnit.MILLISECONDS);
                }
                Log.i(TAG, "retry when throw");
                // Max retries hit. Pass an error so the chain is forcibly completed
                // only onNext triggers a re-subscription (onError + onComplete kills it)
                return Observable.error(throwable);
            }
        });
    }
}

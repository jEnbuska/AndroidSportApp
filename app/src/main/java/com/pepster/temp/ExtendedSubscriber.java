package com.pepster.temp;

import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by WinNabuska on 30.3.2016.
 */
public abstract class ExtendedSubscriber<R> extends Subscriber<R> implements Cloneable {

    protected Action0 mOnStart = () -> {};
    protected Action1<? super R> mOnNext=n -> {};
    protected Action1<Throwable> mOnError= e -> {};
    protected Action0 mOnComplete = () -> {};

    public static <R>ExtendedSubscriber<R> from(final Action0 onStart,
                                                final Action1<? super R> onNext,
                                                final Action1<Throwable> onError,
                                                final Action0 onComplete){
        ExtendedSubscriber<R> instance = new ExtendedSubscriber<R>() {
            @Override
            public void onStart() {
                this.mOnStart.call();
            }
            @Override
            public void onNext(R r) {
                this.mOnNext.call(r);
            }
            @Override
            public void onCompleted() {
                this.mOnComplete.call();
            }
            @Override
            public void onError(Throwable e) {
                this.mOnError.call(e);
            }
        };
        instance.mOnStart=onStart;
        instance.mOnComplete = onComplete;
        instance.mOnNext = onNext;
        instance.mOnError = onError;
        return instance;
    }

    public static <R>ExtendedSubscriber<R> from(final Action1<? super R> onNext){
        ExtendedSubscriber<R> instance = new ExtendedSubscriber<R>() {
            @Override
            public void onStart() {
                this.mOnStart.call();
            }
            @Override
            public void onNext(R r) {
                this.mOnNext.call(r);
            }
            @Override
            public void onCompleted() {
                this.mOnComplete.call();
            }
            @Override
            public void onError(Throwable e) {
                this.mOnError.call(e);
            }
        };
        instance.mOnNext = onNext;
        return instance;
    }

    public abstract void onStart();

    /*public static interface OnStartObserver<V>{
        void onStart();
    }*/
}

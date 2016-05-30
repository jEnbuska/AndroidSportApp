package com.pepster.utilities;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by WinNabuska on 3.4.2016.
 *
 http://stackoverflow.com/questions/36385194/rxjava-pausablebuffer?noredirect=1#comment60393813_36385194
 *
 */
public final class ContinueWhen<T> implements Observable.Operator<T, T> {

    final Func1<T, Boolean> continuePredicate;



    private ContinueWhen(Func1<T, Boolean> continuePredicate) {
        this.continuePredicate = continuePredicate;
    }

    public static <T>ContinueWhen<T> create(Func1<T, Boolean> whileTrue){
        return new ContinueWhen<>(whileTrue);
    }

    @Override
    public Subscriber<? super T> call(Subscriber<? super T> child) {
        ContinueWhileSubscriber parent = new ContinueWhileSubscriber(child);
        child.add(parent);
        return parent;
    }

    final class ContinueWhileSubscriber extends Subscriber<T> {

        final Subscriber<? super T> actual;
        Deque<T> buffer = new LinkedBlockingDeque<>();

        public ContinueWhileSubscriber(Subscriber<? super T> actual) {
            this.actual = actual;
        }

        @Override
        public void onNext(T t) {
            buffer.add(t);
            if (continuePredicate.call(t)) {
                while(!buffer.isEmpty())
                    actual.onNext(buffer.poll());
            }
        }

        @Override
        public void onError(Throwable e) {
            buffer = null;
            actual.onError(e);
        }

        @Override
        public void onCompleted() {
            while (!buffer.isEmpty())
                actual.onNext(buffer.poll());
            buffer=null;
            actual.onCompleted();
        }
    }
}

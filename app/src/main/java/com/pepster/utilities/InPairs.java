package com.pepster.utilities;

import android.support.v4.util.Pair;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by WinNabuska on 22.4.2016.
 */
/**@see <a href="https://github.com/ReactiveX/RxJava/wiki/Implementing-Your-Own-Operators">RxJava wiki: Implementing Your Own Operators</a>
 *
 * InPairs can be used to make an observable emit items in pairs instead of single items
 * Let's say the observable emits items 'a... b... c... d'. When you pass the stream of items through InPairs
 * using lift like this: someObservable.lift(InPairs.create), then items onward will be emited like
 * this: {a, b}... {b, c}... {c, d}.
 * You can use this class forexample like this:
 * Observable.from(strList).lift(InPairs.create()).forEach(pair -> Log.i(TAG, pair.first + pair.second)*/
public class InPairs<T> implements Observable.Operator<Pair<T, T>, T> {

    private InPairs() {
    }

    public static <T> InPairs<T> create() {
        return new InPairs<>();
    }


    @Override
    public Subscriber<? super T> call(Subscriber<? super Pair<T, T>> subscriber) {
        InParesSubscriber parent = new InParesSubscriber(subscriber);
        subscriber.add(parent);
        return parent;
    }

    final class InParesSubscriber extends Subscriber<T> {

        final Subscriber<? super Pair<T,T>> actual;
        T previous;

        public InParesSubscriber(Subscriber<? super Pair<T, T>> actual) {
            this.actual = actual;
        }

        @Override
        public void onNext(T t) {
            //does not sen the first item with a null pare
            if(previous!=null){
                actual.onNext(Pair.create(previous, t));
            }
            previous = t;
        }

        @Override
        public void onError(Throwable e) {
            previous = null;
            actual.onError(e);
        }

        @Override
        public void onCompleted() {
            previous = null;
            actual.onCompleted();
        }
    }
}

package com.pepster.temp;

import java.util.Collection;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by WinNabuska on 23.4.2016.
 */
public class ToKeyValueGroups<K, V> implements Observable.Operator<Map.Entry<K,Collection<V>>, Map<K,Collection<V>>> {

    private ToKeyValueGroups() {
    }

    public static <K, V>ToKeyValueGroups<K, V> create(){
        return new ToKeyValueGroups<>();
    }

    @Override
    public Subscriber<? super Map<K, Collection<V>>> call(Subscriber<? super Map.Entry<K, Collection<V>>> subscriber) {
        ContinueWhileSubscriber parent = new ContinueWhileSubscriber(subscriber);
        subscriber.add(parent);
        return parent;
    }

    final class ContinueWhileSubscriber extends Subscriber<Map<K, Collection<V>>> {

        final Subscriber<? super Map.Entry<K, Collection<V>>> actual;

        public ContinueWhileSubscriber(Subscriber<? super Map.Entry<K, Collection<V>>> actual) {
            this.actual = actual;
        }

        @Override
        public void onNext(Map<K, Collection<V>> t) {
            for(Map.Entry<K,Collection<V>> entry : t.entrySet()){//probably emits just one
                actual.onNext(entry);
            }
        }

        @Override
        public void onError(Throwable e) {
            actual.onError(e);
        }

        @Override
        public void onCompleted() {
            actual.onCompleted();
        }
    }
}

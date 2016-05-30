package com.pepster.data;

import com.pepster.Interfaces.UnSubscribePropagable;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by WinNabuska on 20.4.2016.
 * Thread safe HashMap Proxy that provides an observable of the map
 * Every time time a value is change, added or removed on the map,
 * the map is emitted to everyone that is subscribed to the observable
 */
public class ReactiveHashMap<K, V> implements UnSubscribePropagable {

    private static final String TAG = ReactiveHashMap.class.getSimpleName();

    final private Map<K, V> mMap;
    final private Subject<Map<K,V>, Map<K, V>> mMapSubject;

    public Observable<Map<K, V>> observable(){
        return mMapSubject.asObservable();
    }

    public Map<K, V> getMap(){
        return mMap;
    }

    public synchronized V get(K key){
        return mMap.get(key);
    }

    public int size(){
        return mMap.size();
    }

    public synchronized void putAll(Map<K,V> map){
        mMap.putAll(map);
        mMapSubject.onNext(mMap);
    }

    public synchronized void clearAndPutAll(Map<K,V> map){
        mMap.clear();
        mMap.putAll(map);
        mMapSubject.onNext(mMap);
    }

    public synchronized void clear(){
        mMap.clear();
        mMapSubject.onNext(mMap);
    }

    public synchronized V put(K key, V value){
        V v = mMap.put(key,value);
        mMapSubject.onNext(mMap);
        return v;
    }

    public synchronized V remove(K key){
        V value = mMap.remove(key);
        mMapSubject.onNext(mMap);
        return value;
    }

    /**
     * simnple copy of Java 8:s corresponding
     * https://docs.oracle.com/javase/8/docs/api/java/util/Map.html#computeIfAbsent-K-java.util.function.Function-*/
    public synchronized V computeIfAbsent(K key, Func1<? super K, ? extends V> mappingFunction){
        if (mMap.get(key) == null) {
            V newValue = mappingFunction.call(key);
            if (newValue != null)
                return mMap.put(key, newValue);
        }
        return null;
    }
    /**
     * this is a simple copy of Java 8:s corresponding
     * https://docs.oracle.com/javase/8/docs/api/java/util/Map.html#computeIfPresent-K-java.util.function.BiFunction-*/
    public synchronized V computeIfPresent(K key, Func2<? super K,? super V,? extends V> remappingFunction){
        if (mMap.get(key) != null) {
            V oldValue = mMap.get(key);
            V newValue = remappingFunction.call(key, oldValue);
            if (newValue != null)
                return mMap.put(key, newValue);
            else
                return mMap.remove(key);
        }
        return null;
    }

    public boolean containsKey(K key){
        return mMap.containsKey(key);
    }
    public boolean containsValue(V value){
        return mMap.containsValue(value);
    }



    @Override
    public synchronized boolean equals(Object o) {
        if(o instanceof ReactiveHashMap){
            ReactiveHashMap other =(ReactiveHashMap) o;
            return other.mMap.equals(mMap);
        }
        return false;
    }

    public ReactiveHashMap(Map<K,V> map){
        mMap = new ConcurrentHashMap<>(map);
        mMapSubject = new SerializedSubject<>(BehaviorSubject.create());
    }

    public ReactiveHashMap(){
        this.mMap = new ConcurrentHashMap<>();
        mMapSubject = new SerializedSubject<>(BehaviorSubject.create());
    }

    @Override
    public void propagateUnSubscribe() {
        mMapSubject.onCompleted();
    }

    /**returns the keys that only THIS our map CONTAINS*/
    public synchronized Set<K> leftOuterJoin(Map<K, ?> map){
        Set<K> keys = new TreeSet<>(mMap.keySet());
        keys.removeAll(map.keySet());
        return keys;
    }
    public synchronized Set<K> leftOuterJoin(ReactiveHashMap<K, ?> rMap){
        return leftOuterJoin(rMap.mMap);
    }

    /**returns the keys that only the OTHER map CONTAINS*/
    public synchronized Set<K> rightOuterJoin(Map<K, ?> map){
        Set<K> keys = new TreeSet<>(map.keySet());
        keys.removeAll(mMap.keySet());
        return keys;
    }
    public synchronized Set<K> rightOuterJoin(ReactiveHashMap<K, ?> rMap){
        return rightOuterJoin(rMap.mMap);
    }

    /**returns the keys that BOTH our and other CONTAINS*/
    public synchronized Set<K> innerJoin(Map<K, ?> map){
        Set<K> keys = new TreeSet<>(mMap.keySet());
        keys.removeAll(leftOuterJoin(map));
        keys.removeAll(rightOuterJoin(map));
        return keys;
    }
    public synchronized Set<K> innerJoin(ReactiveHashMap<K, ?> rMap){
        return innerJoin(rMap.mMap);
    }
}
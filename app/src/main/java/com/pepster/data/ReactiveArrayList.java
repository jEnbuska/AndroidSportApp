package com.pepster.data;

import com.pepster.Interfaces.UnSubscribePropagable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by WinNabuska on 20.4.2016.
 *
 * Thread safe ArrayList Proxy that provides an observable of the list
 *  Every time time a value is change, added or removed on the list,
 * the list is emitted to everyone that is subscribed to the observable
 */
public class ReactiveArrayList<R> implements UnSubscribePropagable {

    private final static String TAG = ReactiveArrayList.class.getSimpleName();

    private final List<R> mList;
    private final Subject<List<R>, List<R>> mListSubject;

    public List<R> getList(){
        return mList;
    }

    public R get(int index){
        return mList.get(index);
    }

    public Observable<List<R>> observable(){
        return mListSubject.asObservable();
    }
    public int size(){
        return mList.size();
    }

    public synchronized void clear(){
        mList.clear();
        mListSubject.onNext(mList);
    }

    public synchronized boolean clearAndAddAll(Collection<R> values){
        mList.clear();
        boolean success = mList.addAll(values);
        mListSubject.onNext(mList);
        return success;
    }

    public synchronized boolean addAll(Collection<R> values){
        boolean success = mList.addAll(values);
        mListSubject.onNext(mList);
        return success;
    }

    public synchronized boolean addAll(R ...values){
        boolean success = mList.addAll(Arrays.asList(values));
        mListSubject.onNext(mList);
        return success;
    }

    public synchronized boolean add(R value){
        boolean success = mList.add(value);
        mListSubject.onNext(mList);
        return success;
    }

    public synchronized boolean remove(R value){
        boolean success = mList.remove(value);
        if(success)
            mListSubject.onNext(mList);
        return success;
    }

    public synchronized R remove(int index){
        R v = mList.remove(index);
        mListSubject.onNext(mList);
        return v;
    }

    @Override
    public synchronized boolean equals(Object o) {
        if(o instanceof ReactiveArrayList){
            ReactiveArrayList other =(ReactiveArrayList) o;
            return other.mList.equals(mList);
        }
        return false;
    }

    public synchronized boolean removeAll(Collection<R> values){
        boolean v = mList.removeAll(values);
        mListSubject.onNext(mList);
        return v;
    }

    public synchronized R set(int index, R value){
        R v = mList.set(index,value);
        mListSubject.onNext(mList);
        return v;
    }


    public static <R>ReactiveArrayList<R> create(){
        return new ReactiveArrayList<>();
    }
    public static <R>ReactiveArrayList<R> create(R ...values){
        ReactiveArrayList obsList = new ReactiveArrayList<>();
        obsList.mList.addAll(Arrays.asList(values));
        return obsList;
    }
    public static <R>ReactiveArrayList<R> create(Collection<R> values){
        ReactiveArrayList obsList = new ReactiveArrayList<>();
        obsList.mList.addAll(values);
        return obsList;
    }

    public ReactiveArrayList(){
        mList = new CopyOnWriteArrayList<>();
        mListSubject = new SerializedSubject<>(BehaviorSubject.create());
    }

    @Override
    public void propagateUnSubscribe() {
        mListSubject.onCompleted();
    }
}

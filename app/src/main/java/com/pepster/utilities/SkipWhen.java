package com.pepster.utilities;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func2;

/**
 * Created by WinNabuska on 22.4.2016.
 *
 * Skip when emits the first item and after that it will skip all the items that do not pass
 * the predicate. Ones the next item passes the comparison the reference to the first item is
 * removed and replaced by the next item.
 * Lets say our SkipWhen is of type Integer and we have an integer predicate:
 * (first, second) -> second-first<3
 * when we pass the stream of observable data through SkipWhen with like so:
 *
 * Observable.just(1,2,3,4,5,6,7,8, ... ).lift(SkipWhen.create((first, second) -> second-first<3))
 * .subscribe();
 *
 * The execution of the program should go something like this:
 *
 * 1 -> emit
 * (2-1)<3 - skip
 * (3-1)<3 - skip
 * (4-3)<3 -> emit
 * (5-4)<3 - skip
 * (6-4)<3 - skip
 * (7-4)<3 -> emit
 * (8-9)<3 - skip
 *  ...
 */
public final class SkipWhen<T> implements Observable.Operator<T, T> {

    final Func2<T,T, Boolean> predicate;

    private SkipWhen(Func2<T,T, Boolean> predicate) {
        this.predicate = predicate;
    }

    public static <T>SkipWhen<T> create(Func2<T,T, Boolean> predicate){
        return new SkipWhen<>(predicate);
    }

    @Override
    public Subscriber<? super T> call(Subscriber<? super T> child) {
        SkipWhenSubscriber parent = new SkipWhenSubscriber(child);
        child.add(parent);
        return parent;
    }

    final class SkipWhenSubscriber extends Subscriber<T> {

        final Subscriber<? super T> actual;
        T previous;

        public SkipWhenSubscriber(Subscriber<? super T> actual) {
            this.actual = actual;
        }

        @Override
        public void onNext(T t) {
            if(previous==null || !predicate.call(previous, t)) {
                actual.onNext(t);
                previous = t;
            }
            if(previous !=null && !predicate.call(previous, t)){
                previous = t;
                actual.onNext(t);
            }
        }

        @Override
        public void onError(Throwable e) {
            previous = null;
            actual.onError(e);
        }

        @Override
        public void onCompleted() {
            previous=null;
            actual.onCompleted();
        }
    }
}

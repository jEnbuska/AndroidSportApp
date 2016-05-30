package com.pepster.utilities;

import com.pepster.utilities.SkipWhen;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

import static org.junit.Assert.*;

/**
 * Created by WinNabuska on 23.4.2016.
 */
@RunWith(JUnit4.class)
public class SkipWhenTest {

    @Test
    public void skipTest(){
        List<Integer> ints = Observable.just(1,2,3,4,5,6).lift(SkipWhen.create((first, second) -> second-first<2))
                .toList().toBlocking().single();

        assertEquals(Arrays.asList(1,3,5),ints);
    }

}
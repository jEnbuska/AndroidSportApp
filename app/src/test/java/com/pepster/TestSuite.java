package com.pepster;

import com.pepster.data.MapContent;
import com.pepster.data.MapContentTest;
import com.pepster.utilities.SkipWhenTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by WinNabuska on 23.4.2016.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        MapContentTest.class,
        SkipWhenTest.class
})
public class TestSuite {
}

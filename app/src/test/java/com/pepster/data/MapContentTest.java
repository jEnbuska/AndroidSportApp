package com.pepster.data;

import android.test.mock.MockContext;

import com.pepster.utilities.AdaptiveLocation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by WinNabuska on 23.4.2016.
 */
@RunWith(JUnit4.class)
public class MapContentTest {

    MapContent contentA, contentB;
    String titleA = "titleA", titleB = "titleB";
    List<AdaptiveLocation>
            routeA = Arrays.asList(new AdaptiveLocation(1,1), new AdaptiveLocation(2,2)),
            routeB = Arrays.asList(new AdaptiveLocation(1,1), new AdaptiveLocation(4,3));
    long modTimeA = 1, modTimeB = 2, lastUsageA = 3, lastUsageB = 4;
    List<PepPoint> pepPointsA = new LinkedList<>(), pepPointsB = new LinkedList<>();
    PepPoint pp1 = new PepPoint(), newPp1 = new PepPoint(), pp2 = new PepPoint() , newPp2 = new PepPoint(), pp3 = new PepPoint(), pp4 = new PepPoint();

    @Before
    public void setUp() throws Exception {
        PepPoint.initContext(new MockContext());
        contentA=MapContent.createTestObject();
        contentA.ID("X");
        contentA.setTitle(titleA);
        contentA.setModTime(modTimeA);
        contentA.setLastUsage(lastUsageA);
        contentA.setRoute(routeA);

        contentB=MapContent.createTestObject();
        contentB.ID("X");
        contentB.setTitle(titleB);
        contentB.setModTime(modTimeB);
        contentB.setLastUsage(lastUsageB);
        contentB.setRoute(routeB);

        pp1.initialize("1");
        pp1.setPreconditions(new ArrayList<>());
        pp1.setType(PepPoint.TYPE_CUSTOM);
        pp1.setLanguage("fi_FI");
        pp1.setLocation(new AdaptiveLocation(1,1));
        pp1.setMessage("1");
        pp1.setTriggerRadius(1);

        newPp1.initialize("1");
        newPp1.ID("1");
        newPp1.setPreconditions(Arrays.asList("2"));
        newPp1.setType(PepPoint.TYPE_DIRECTION);
        newPp1.setLocation(new AdaptiveLocation(20,20));
        newPp1.setMessage("1");
        newPp1.setLanguage("fi_FI");
        newPp1.setTriggerRadius(1);

        pp2.initialize("2");
        pp2.setPreconditions(Arrays.asList("3"));
        pp2.setType(PepPoint.TYPE_CUSTOM);
        pp2.setLanguage("en_US");
        pp2.setLocation(new AdaptiveLocation(1,1));
        pp2.setMessage("2");
        pp2.setTriggerRadius(2);

        newPp2.initialize("2");
        newPp2.setPreconditions(new ArrayList<>());
        newPp2.setType(PepPoint.TYPE_DIRECTION);
        newPp2.setLocation(new AdaptiveLocation(1,1));
        newPp2.setMessage("2");
        newPp2.setLanguage("fi_FI");
        newPp2.setTriggerRadius(1);

        pp3.initialize("3");
        pp3.setPreconditions(Arrays.asList("2"));
        pp3.setType(PepPoint.TYPE_CUSTOM);
        pp3.setLanguage("en_US");
        pp3.setLocation(new AdaptiveLocation(1,1));
        pp3.setMessage("3");
        pp3.setTriggerRadius(2);

        pp4.initialize("4");
        pp4.setPreconditions(Arrays.asList("2"));
        pp4.setType(PepPoint.TYPE_CUSTOM);
        pp4.setLanguage("en_US");
        pp4.setLocation(new AdaptiveLocation(1,1));
        pp4.setMessage("4");
        pp4.setTriggerRadius(2);

        pepPointsA.addAll(Arrays.asList(pp1,pp2,pp3));
        Map<String, PepPoint> pepPointsMapA = new TreeMap<>();
        for (PepPoint pepPoint : pepPointsA) {
            pepPointsMapA.put(pepPoint.ID(), pepPoint);
        }
        contentA.setPepPoints(pepPointsMapA);

        pepPointsB.addAll(Arrays.asList(newPp1,newPp2,pp4));
        Map<String, PepPoint> pepPointsMapB = new TreeMap<>();
        for (PepPoint pepPoint : pepPointsB) {
            pepPointsMapB.put(pepPoint.ID(),pepPoint);
        }
        contentB.setPepPoints(pepPointsMapB);
    }

    @Test
    public void testUpdateObservable_messagesGettingThrough() throws Exception {
        List<String> updateMessages = new LinkedList<>();
        contentA.updateObservable().subscribeOn(Schedulers.immediate()).subscribe(m -> updateMessages.add(m));
        Thread.sleep(100);
        contentA.updateWith(contentB);
        Thread.sleep(500);

        assertTrue(updateMessages.contains("title changed"));
        assertTrue(updateMessages.contains("route changed"));
        assertTrue(updateMessages.contains("peppoint map changed"));/*twice both on remove and add*/
        assertTrue(updateMessages.contains("peppoint 1 type changed"));
        assertTrue(updateMessages.contains("peppoint 1 location changed "));
        assertTrue(updateMessages.contains("peppoint 1 preconditions changed"));
        assertTrue(updateMessages.contains("peppoint 2 type changed"));
        assertTrue(updateMessages.contains("peppoint 2 language changed"));
        assertTrue(updateMessages.contains("peppoint 2 radius changed"));
        assertTrue(updateMessages.contains("peppoint 2 preconditions changed"));
        assertEquals(11, updateMessages.size());
    }

    @Test
    public void testUpdateObservable_noExtraMessagesGettingThrough() throws Exception {
        MapContent content = MapContent.createTestObject2();//Empty in all ways
        Set<String> messages = new HashSet<>();
        content.updateObservable().subscribe(m -> messages.add(m));
        content.setLastUsage(1);
        content.setTitle("a");
        content.setModTime(2);

        PepPoint pepPoint = new PepPoint();
        Map<String, PepPoint> map = new HashMap<>();
        map.put("1", pepPoint);
        content.setPepPoints(map);

        List<AdaptiveLocation> route = new LinkedList<>();
        route.add(new AdaptiveLocation(1,1));
        content.setRoute(route);
        pepPoint.setType("abcde");
        pepPoint.setTriggerRadius(50);
        pepPoint.setMessage("msg");
        pepPoint.setLanguage("saksa");
        pepPoint.setLocation(new AdaptiveLocation(1,1));
        pepPoint.setPreconditions(new ArrayList<>());
        assertEquals("[]", Arrays.toString(messages.toArray()));
    }

    @Test
    public void testUpdate() throws Exception {
        Thread.sleep(20);
        contentA.updateWith(contentB);
        assertEquals(3, contentA.getPepPoints().size());
        assertTrue(contentA.pepPoints().containsKey("1"));
        assertTrue(contentA.pepPoints().containsKey("2"));
        assertFalse(contentA.pepPoints().containsKey("3"));
        assertTrue(contentA.pepPoints().containsKey("4"));
        assertEquals(1, contentA.getPepPoints().get("1").getPreconditions().size());
        assertEquals("2", contentA.getPepPoints().get("1").getPreconditions().get(0));
        assertEquals(PepPoint.TYPE_DIRECTION, contentA.getPepPoints().get("1").getType());
        assertEquals(contentA.getPepPoints().get("1"), contentB.getPepPoints().get("1"));
        assertEquals(contentA.getPepPoints().get("2"), contentB.getPepPoints().get("2"));
        assertEquals(contentA.getPepPoints().get("4"), contentB.getPepPoints().get("4"));
        assertEquals(contentA, contentB);
    }
}
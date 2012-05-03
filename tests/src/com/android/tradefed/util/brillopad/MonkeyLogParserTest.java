/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tradefed.util.brillopad;

import com.android.tradefed.util.brillopad.item.AnrItem;
import com.android.tradefed.util.brillopad.item.JavaCrashItem;
import com.android.tradefed.util.brillopad.item.MonkeyLogItem;
import com.android.tradefed.util.brillopad.item.MonkeyLogItem.DroppedCategory;

import junit.framework.TestCase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Unit tests for {@link MonkeyLogParser}
 */
public class MonkeyLogParserTest extends TestCase {

    /**
     * Test that a monkey can be parsed if there are no crashes.
     */
    public void testParse_success() throws ParseException {
        List<String> lines = Arrays.asList(
                "# Wednesday, 04/25/2012 01:37:12 AM - device uptime = 242.13: Monkey command used for this test:",
                "adb shell monkey -p com.google.android.browser  -c android.intent.category.SAMPLE_CODE -c android.intent.category.CAR_DOCK -c android.intent.category.LAUNCHER -c android.intent.category.MONKEY -c android.intent.category.INFO  --ignore-security-exceptions --throttle 100  -s 528 -v -v -v 10000 ",
                "",
                ":Monkey: seed=528 count=10000",
                ":AllowPackage: com.google.android.browser",
                ":IncludeCategory: android.intent.category.LAUNCHER",
                ":Switch: #Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;launchFlags=0x10200000;component=com.google.android.browser/com.android.browser.BrowserActivity;end",
                "    // Allowing start of Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] cmp=com.google.android.browser/com.android.browser.BrowserActivity } in package com.google.android.browser",
                "Sleeping for 100 milliseconds",
                ":Sending Key (ACTION_DOWN): 23    // KEYCODE_DPAD_CENTER",
                ":Sending Key (ACTION_UP): 23    // KEYCODE_DPAD_CENTER",
                "Sleeping for 100 milliseconds",
                ":Sending Trackball (ACTION_MOVE): 0:(-5.0,3.0)",
                ":Sending Trackball (ACTION_MOVE): 0:(3.0,3.0)",
                ":Sending Trackball (ACTION_MOVE): 0:(-1.0,3.0)",
                ":Sending Trackball (ACTION_MOVE): 0:(4.0,-2.0)",
                ":Sending Trackball (ACTION_MOVE): 0:(1.0,4.0)",
                ":Sending Trackball (ACTION_MOVE): 0:(-4.0,2.0)",
                "    //[calendar_time:2012-04-25 01:42:20.140  system_uptime:535179]",
                "    // Sending event #9900",
                ":Sending Trackball (ACTION_MOVE): 0:(2.0,-4.0)",
                ":Sending Trackball (ACTION_MOVE): 0:(-2.0,0.0)",
                ":Sending Trackball (ACTION_MOVE): 0:(2.0,2.0)",
                ":Sending Trackball (ACTION_MOVE): 0:(-5.0,4.0)",
                "Events injected: 10000",
                ":Dropped: keys=5 pointers=6 trackballs=7 flips=8 rotations=9",
                "// Monkey finished",
                "",
                "# Wednesday, 04/25/2012 01:42:09 AM - device uptime = 539.21: Monkey command ran for: 04:57 (mm:ss)",
                "",
                "----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        MonkeyLogItem monkeyLog = new MonkeyLogParser().parse(lines);
        assertNotNull(monkeyLog);
        assertEquals(parseTime("2012-04-25 01:37:12"), monkeyLog.getStartTime());
        assertEquals(parseTime("2012-04-25 01:42:09"), monkeyLog.getStopTime());
        assertEquals(1, monkeyLog.getPackages().size());
        assertTrue(monkeyLog.getPackages().contains("com.google.android.browser"));
        assertEquals(1, monkeyLog.getCategories().size());
        assertTrue(monkeyLog.getCategories().contains("android.intent.category.LAUNCHER"));
        assertEquals(100, monkeyLog.getThrottle());
        assertEquals(528, monkeyLog.getSeed().intValue());
        assertEquals(10000, monkeyLog.getTargetCount().intValue());
        assertTrue(monkeyLog.getIgnoreSecurityExceptions());
        assertEquals(4 * 60 * 1000 + 57 * 1000, monkeyLog.getTotalDuration().longValue());
        assertEquals(242130, monkeyLog.getStartUptimeDuration().longValue());
        assertEquals(539210, monkeyLog.getStopUptimeDuration().longValue());
        assertTrue(monkeyLog.isFinished());
        assertEquals(9900, monkeyLog.getIntermediateCount());
        assertEquals(10000, monkeyLog.getFinalCount().intValue());
        assertEquals(5, monkeyLog.getDroppedCount(DroppedCategory.KEYS).intValue());
        assertEquals(6, monkeyLog.getDroppedCount(DroppedCategory.POINTERS).intValue());
        assertEquals(7, monkeyLog.getDroppedCount(DroppedCategory.TRACKBALLS).intValue());
        assertEquals(8, monkeyLog.getDroppedCount(DroppedCategory.FLIPS).intValue());
        assertEquals(9, monkeyLog.getDroppedCount(DroppedCategory.ROTATIONS).intValue());
        assertNull(monkeyLog.getCrash());
    }

    /**
     * Test that a monkey can be parsed if there is an ANR.
     */
    public void testParse_anr() throws ParseException {
        List<String> lines = Arrays.asList(
                "# Tuesday, 04/24/2012 05:23:30 PM - device uptime = 216.48: Monkey command used for this test:",
                "adb shell monkey -p com.google.android.youtube  -c android.intent.category.SAMPLE_CODE -c android.intent.category.CAR_DOCK -c android.intent.category.LAUNCHER -c android.intent.category.MONKEY -c android.intent.category.INFO  --ignore-security-exceptions --throttle 100  -s 993 -v -v -v 10000 ",
                "",
                ":Monkey: seed=993 count=10000",
                ":AllowPackage: com.google.android.youtube",
                ":IncludeCategory: android.intent.category.LAUNCHER",
                ":Switch: #Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;launchFlags=0x10200000;component=com.google.android.youtube/.app.honeycomb.Shell%24HomeActivity;end",
                "    // Allowing start of Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] cmp=com.google.android.youtube/.app.honeycomb.Shell$HomeActivity } in package com.google.android.youtube",
                "Sleeping for 100 milliseconds",
                ":Sending Key (ACTION_UP): 21    // KEYCODE_DPAD_LEFT",
                "Sleeping for 100 milliseconds",
                ":Sending Key (ACTION_DOWN): 22    // KEYCODE_DPAD_RIGHT",
                ":Sending Key (ACTION_UP): 22    // KEYCODE_DPAD_RIGHT",
                "    //[calendar_time:2012-04-25 00:27:27.155  system_uptime:454996]",
                "    // Sending event #5300",
                ":Sending Key (ACTION_UP): 19    // KEYCODE_DPAD_UP",
                "Sleeping for 100 milliseconds",
                ":Sending Trackball (ACTION_MOVE): 0:(4.0,3.0)",
                ":Sending Key (ACTION_DOWN): 20    // KEYCODE_DPAD_DOWN",
                ":Sending Key (ACTION_UP): 20    // KEYCODE_DPAD_DOWN",
                "// NOT RESPONDING: com.google.android.youtube (pid 3301)",
                "ANR in com.google.android.youtube (com.google.android.youtube/.app.honeycomb.phone.WatchActivity)",
                "Reason: keyDispatchingTimedOut",
                "Load: 1.0 / 1.05 / 0.6",
                "CPU usage from 4794ms to -1502ms ago with 99% awake:",
                "  18% 3301/com.google.android.youtube: 16% user + 2.3% kernel / faults: 268 minor 9 major",
                "  13% 313/system_server: 9.2% user + 4.4% kernel / faults: 906 minor 3 major",
                "  10% 117/surfaceflinger: 4.9% user + 5.5% kernel / faults: 1 minor",
                "  10% 120/mediaserver: 6.8% user + 3.6% kernel / faults: 1189 minor",
                "34% TOTAL: 19% user + 13% kernel + 0.2% iowait + 1% softirq",
                "",
                "** Monkey aborted due to error.",
                "Events injected: 5322",
                ":Sending rotation degree=0, persist=false",
                ":Dropped: keys=1 pointers=0 trackballs=0 flips=0 rotations=0",
                "## Network stats: elapsed time=252942ms (0ms mobile, 252942ms wifi, 0ms not connected)",
                "** System appears to have crashed at event 5322 of 10000 using seed 993",
                "",
                "# Tuesday, 04/24/2012 05:27:44 PM - device uptime = 471.37: Monkey command ran for: 04:14 (mm:ss)",
                "",
                "----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------",
                "");

        MonkeyLogItem monkeyLog = new MonkeyLogParser().parse(lines);
        assertNotNull(monkeyLog);
        assertEquals(parseTime("2012-04-24 17:23:30"), monkeyLog.getStartTime());
        assertEquals(parseTime("2012-04-24 17:27:44"), monkeyLog.getStopTime());
        assertEquals(1, monkeyLog.getPackages().size());
        assertTrue(monkeyLog.getPackages().contains("com.google.android.youtube"));
        assertEquals(1, monkeyLog.getCategories().size());
        assertTrue(monkeyLog.getCategories().contains("android.intent.category.LAUNCHER"));
        assertEquals(100, monkeyLog.getThrottle());
        assertEquals(993, monkeyLog.getSeed().intValue());
        assertEquals(10000, monkeyLog.getTargetCount().intValue());
        assertTrue(monkeyLog.getIgnoreSecurityExceptions());
        assertEquals(4 * 60 * 1000 + 14 * 1000, monkeyLog.getTotalDuration().longValue());
        assertEquals(216480, monkeyLog.getStartUptimeDuration().longValue());
        assertEquals(471370, monkeyLog.getStopUptimeDuration().longValue());
        assertFalse(monkeyLog.isFinished());
        assertEquals(5300, monkeyLog.getIntermediateCount());
        assertEquals(5322, monkeyLog.getFinalCount().intValue());
        assertNotNull(monkeyLog.getCrash());
        assertTrue(monkeyLog.getCrash() instanceof AnrItem);
        assertEquals("com.google.android.youtube", monkeyLog.getCrash().getApp());
        assertEquals(3301, monkeyLog.getCrash().getPid().intValue());
        assertEquals("keyDispatchingTimedOut", ((AnrItem) monkeyLog.getCrash()).getReason());
    }

    /**
     * Test that a monkey can be parsed if there is a JavaCrash.
     */
    public void testParse_java_crash() throws ParseException {
        List<String> lines = Arrays.asList(
                "# Tuesday, 04/24/2012 05:05:50 PM - device uptime = 232.65: Monkey command used for this test:",
                "adb shell monkey -p com.google.android.apps.maps  -c android.intent.category.SAMPLE_CODE -c android.intent.category.CAR_DOCK -c android.intent.category.LAUNCHER -c android.intent.category.MONKEY -c android.intent.category.INFO  --ignore-security-exceptions --throttle 100  -s 501 -v -v -v 10000 ",
                "",
                ":Monkey: seed=501 count=10000",
                ":AllowPackage: com.google.android.apps.maps",
                ":IncludeCategory: android.intent.category.LAUNCHER",
                ":Switch: #Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;launchFlags=0x10200000;component=com.google.android.apps.maps/com.google.android.maps.LatitudeActivity;end",
                "    // Allowing start of Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] cmp=com.google.android.apps.maps/com.google.android.maps.LatitudeActivity } in package com.google.android.apps.maps",
                "Sleeping for 100 milliseconds",
                ":Sending Touch (ACTION_DOWN): 0:(332.0,70.0)",
                ":Sending Touch (ACTION_UP): 0:(332.55292,76.54678)",
                "    //[calendar_time:2012-04-25 00:06:38.419  system_uptime:280799]",
                "    // Sending event #1600",
                ":Sending Touch (ACTION_MOVE): 0:(1052.2666,677.64594)",
                ":Sending Touch (ACTION_UP): 0:(1054.7593,687.3757)",
                "Sleeping for 100 milliseconds",
                "// CRASH: com.google.android.apps.maps (pid 3161)",
                "// Short Msg: java.lang.Exception",
                "// Long Msg: java.lang.Exception: This is the message",
                "// Build Label: google/yakju/maguro:JellyBean/JRN24B/338896:userdebug/dev-keys",
                "// Build Changelist: 338896",
                "// Build Time: 1335309051000",
                "// java.lang.Exception: This is the message",
                "// \tat class.method1(Class.java:1)",
                "// \tat class.method2(Class.java:2)",
                "// \tat class.method3(Class.java:3)",
                "// ",
                "** Monkey aborted due to error.",
                "Events injected: 1649",
                ":Sending rotation degree=0, persist=false",
                ":Dropped: keys=0 pointers=0 trackballs=0 flips=0 rotations=0",
                "## Network stats: elapsed time=48897ms (0ms mobile, 48897ms wifi, 0ms not connected)",
                "** System appears to have crashed at event 1649 of 10000 using seed 501",
                "",
                "# Tuesday, 04/24/2012 05:06:40 PM - device uptime = 282.53: Monkey command ran for: 00:49 (mm:ss)",
                "",
                "----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------",
                "");

        MonkeyLogItem monkeyLog = new MonkeyLogParser().parse(lines);
        assertNotNull(monkeyLog);
        assertEquals(parseTime("2012-04-24 17:05:50"), monkeyLog.getStartTime());
        assertEquals(parseTime("2012-04-24 17:06:40"), monkeyLog.getStopTime());
        assertEquals(1, monkeyLog.getPackages().size());
        assertTrue(monkeyLog.getPackages().contains("com.google.android.apps.maps"));
        assertEquals(1, monkeyLog.getCategories().size());
        assertTrue(monkeyLog.getCategories().contains("android.intent.category.LAUNCHER"));
        assertEquals(100, monkeyLog.getThrottle());
        assertEquals(501, monkeyLog.getSeed().intValue());
        assertEquals(10000, monkeyLog.getTargetCount().intValue());
        assertTrue(monkeyLog.getIgnoreSecurityExceptions());
        assertEquals(49 * 1000, monkeyLog.getTotalDuration().longValue());
        assertEquals(232650, monkeyLog.getStartUptimeDuration().longValue());
        assertEquals(282530, monkeyLog.getStopUptimeDuration().longValue());
        assertFalse(monkeyLog.isFinished());
        assertEquals(1600, monkeyLog.getIntermediateCount());
        assertEquals(1649, monkeyLog.getFinalCount().intValue());
        assertNotNull(monkeyLog.getCrash());
        assertTrue(monkeyLog.getCrash() instanceof JavaCrashItem);
        assertEquals("com.google.android.apps.maps", monkeyLog.getCrash().getApp());
        assertEquals(3161, monkeyLog.getCrash().getPid().intValue());
        assertEquals("java.lang.Exception", ((JavaCrashItem) monkeyLog.getCrash()).getException());
    }

    private Date parseTime(String timeStr) throws ParseException {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.parse(timeStr);
    }
}


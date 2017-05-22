/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.tradefed.result.suite;

import com.android.ddmlib.Log.LogLevel;
import com.android.ddmlib.testrunner.TestResult.TestStatus;
import com.android.ddmlib.testrunner.TestRunResult;
import com.android.tradefed.invoker.IInvocationContext;
import com.android.tradefed.log.LogUtil.CLog;
import com.android.tradefed.result.CollectingTestListener;
import com.android.tradefed.util.TimeUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/** Collect test results for an entire suite invocation and output the final results. */
public class SuiteResultReporter extends CollectingTestListener {

    private long startTime = 0l;
    private long mElapsedTime = 0l;

    private int mTotalModules = 0;
    private int mCompleteModules = 0;

    private long mTotalTests = 0l;
    private long mPassedTests = 0l;
    private long mFailedTests = 0l;
    private long mSkippedTests = 0l;
    private long mAssumeFailureTests = 0l;

    private Map<String, Integer> mModuleExpectedTests = new HashMap<>();
    private Map<String, String> mFailedModule = new HashMap<>();

    @Override
    public void invocationStarted(IInvocationContext context) {
        super.invocationStarted(context);
        startTime = System.currentTimeMillis();
    }

    @Override
    public void testRunStarted(String name, int numTests) {
        super.testRunStarted(name, numTests);
        if (mModuleExpectedTests.get(name) == null) {
            mModuleExpectedTests.put(name, numTests);
        } else {
            mModuleExpectedTests.put(name, mModuleExpectedTests.get(name) + numTests);
        }
    }

    @Override
    public void invocationEnded(long elapsedTime) {
        super.invocationEnded(elapsedTime);
        mElapsedTime = System.currentTimeMillis() - startTime;

        // finalize and print results - general
        Collection<TestRunResult> results = getRunResults();

        mTotalModules = results.size();

        for (TestRunResult moduleResult : results) {
            if (!moduleResult.isRunFailure()) {
                mCompleteModules++;
            } else {
                mFailedModule.put(moduleResult.getName(), moduleResult.getRunFailureMessage());
            }
            mTotalTests += mModuleExpectedTests.get(moduleResult.getName());
            mPassedTests += moduleResult.getNumTestsInState(TestStatus.PASSED);
            mFailedTests += moduleResult.getNumAllFailedTests();
            mSkippedTests += moduleResult.getNumTestsInState(TestStatus.IGNORED);
            mAssumeFailureTests += moduleResult.getNumTestsInState(TestStatus.ASSUMPTION_FAILURE);
        }
        // print a short report summary
        CLog.logAndDisplay(LogLevel.INFO, "============================================");
        CLog.logAndDisplay(LogLevel.INFO, "================= Results ==================");
        printTop10TimeConsumer(results);
        printTop10SlowModules(results);
        CLog.logAndDisplay(LogLevel.INFO, "==========================");
        CLog.logAndDisplay(
                LogLevel.INFO, "Total Run time: %s", TimeUtil.formatElapsedTime(mElapsedTime));
        CLog.logAndDisplay(
                LogLevel.INFO, "%s/%s modules completed", mCompleteModules, mTotalModules);
        if (!mFailedModule.isEmpty()) {
            CLog.logAndDisplay(LogLevel.WARN, "Module(s) with run failure(s):");
            for (Entry<String, String> e : mFailedModule.entrySet()) {
                CLog.logAndDisplay(LogLevel.WARN, "    %s: %s", e.getKey(), e.getValue());
            }
        }
        CLog.logAndDisplay(LogLevel.INFO, "Total Tests       : %s", mTotalTests);
        CLog.logAndDisplay(LogLevel.INFO, "PASSED            : %s", mPassedTests);
        CLog.logAndDisplay(LogLevel.INFO, "FAILED            : %s", mFailedTests);

        if (mSkippedTests > 0l) {
            CLog.logAndDisplay(LogLevel.INFO, "IGNORED           : %s", mSkippedTests);
        }
        if (mAssumeFailureTests > 0l) {
            CLog.logAndDisplay(LogLevel.INFO, "ASSUMPTION_FAILURE: %s", mAssumeFailureTests);
        }

        if (mCompleteModules != mTotalModules) {
            CLog.logAndDisplay(
                    LogLevel.ERROR,
                    "IMPORTANT: Some modules failed to run to completion, tests counts may be"
                            + " inaccurate.");
        }
        CLog.logAndDisplay(LogLevel.INFO, "============== End of Results ==============");
        CLog.logAndDisplay(LogLevel.INFO, "============================================");
    }

    /** Displays the top 10 time consumer for module tests run. */
    void printTop10TimeConsumer(Collection<TestRunResult> results) {
        List<TestRunResult> moduleTime = new ArrayList<>();
        moduleTime.addAll(results);
        Collections.sort(
                moduleTime,
                new Comparator<TestRunResult>() {
                    @Override
                    public int compare(TestRunResult o1, TestRunResult o2) {
                        return (int) (o2.getElapsedTime() - o1.getElapsedTime());
                    }
                });
        int maxModuleDisplay = Math.min(moduleTime.size(), 10);
        CLog.logAndDisplay(
                LogLevel.INFO,
                "============== TOP %s Test Consumer ==============",
                maxModuleDisplay);
        for (int i = 0; i < maxModuleDisplay; i++) {
            CLog.logAndDisplay(
                    LogLevel.INFO,
                    "    %s: %s",
                    moduleTime.get(i).getName(),
                    TimeUtil.formatElapsedTime(moduleTime.get(i).getElapsedTime()));
        }
    }

    /**
     * Display the average tests / second of modules because elapsed is not always relevant. (Some
     * modules have way more test cases than others so only looking at elapsed time is not a good
     * metric for slow modules).
     */
    void printTop10SlowModules(Collection<TestRunResult> results) {
        List<TestRunResult> moduleTime = new ArrayList<>();
        moduleTime.addAll(results);
        // We don't consider module which runs in less than 5 sec.
        for (TestRunResult t : results) {
            if (t.getElapsedTime() < 5000) {
                moduleTime.remove(t);
            }
        }
        Collections.sort(
                moduleTime,
                new Comparator<TestRunResult>() {
                    @Override
                    public int compare(TestRunResult o1, TestRunResult o2) {
                        Float rate1 = ((float) o1.getNumTests() / o1.getElapsedTime());
                        Float rate2 = ((float) o2.getNumTests() / o2.getElapsedTime());
                        return rate1.compareTo(rate2);
                    }
                });
        int maxModuleDisplay = Math.min(moduleTime.size(), 10);
        CLog.logAndDisplay(
                LogLevel.INFO,
                "============== TOP %s Slow Modules ==============",
                maxModuleDisplay);
        for (int i = 0; i < maxModuleDisplay; i++) {
            CLog.logAndDisplay(
                    LogLevel.INFO,
                    "    %s: %.02f tests/sec [%s tests / %s msec]",
                    moduleTime.get(i).getName(),
                    (moduleTime.get(i).getNumTests()
                            / (moduleTime.get(i).getElapsedTime() / 1000f)),
                    moduleTime.get(i).getNumTests(),
                    moduleTime.get(i).getElapsedTime());
        }
    }

    public int getTotalModules() {
        return mTotalModules;
    }

    public int getCompleteModules() {
        return mCompleteModules;
    }

    public long getTotalTests() {
        return mTotalTests;
    }

    public long getPassedTests() {
        return mPassedTests;
    }

    public long getFailedTests() {
        return mFailedTests;
    }
}

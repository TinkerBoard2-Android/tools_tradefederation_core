/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.tradefed.result.proto;

import com.android.annotations.VisibleForTesting;
import com.android.tradefed.build.IBuildInfo;
import com.android.tradefed.invoker.IInvocationContext;
import com.android.tradefed.invoker.InvocationContext;
import com.android.tradefed.invoker.proto.InvocationContext.Context;
import com.android.tradefed.log.LogUtil.CLog;
import com.android.tradefed.metrics.proto.MetricMeasurement.Metric;
import com.android.tradefed.result.ILogSaverListener;
import com.android.tradefed.result.ITestInvocationListener;
import com.android.tradefed.result.LogDataType;
import com.android.tradefed.result.LogFile;
import com.android.tradefed.result.TestDescription;
import com.android.tradefed.result.proto.LogFileProto.LogFileInfo;
import com.android.tradefed.result.proto.TestRecordProto.ChildReference;
import com.android.tradefed.result.proto.TestRecordProto.TestRecord;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/** Parser for the Tradefed results proto format. */
public class ProtoResultParser {

    private ITestInvocationListener mListener;
    private String mCurrentRunName = null;
    /**
     * We don't always want to report the invocation level events again. If we are within an
     * invocation scope we should not report it again.
     */
    private boolean mReportInvocation = false;
    /** The invocation context */
    private IInvocationContext mContext;

    private boolean mQuietParsing = true;

    /** Ctor. */
    public ProtoResultParser(ITestInvocationListener listener, boolean reportInvocation) {
        mListener = listener;
        mReportInvocation = reportInvocation;
    }

    /** Sets whether or not to print when events are received. */
    public void setQuiet(boolean quiet) {
        mQuietParsing = quiet;
    }

    /**
     * Main entry function that takes the finalized completed proto and replay its results.
     *
     * @param finalProto The final {@link TestRecord} to be parsed.
     */
    public void processFinalizedProto(TestRecord finalProto) {
        if (!finalProto.getParentTestRecordId().isEmpty()) {
            throw new IllegalArgumentException("processFinalizedProto only expect a root proto.");
        }

        // Invocation Start
        handleInvocationStart(finalProto);

        evalProto(finalProto.getChildrenList(), false);
        // Invocation End
        handleInvocationEnded(finalProto);
    }

    /**
     * Main entry function where each proto is presented to get parsed into Tradefed events.
     *
     * @param currentProto The current {@link TestRecord} to be parsed.
     */
    public void processNewProto(TestRecord currentProto) {
        // Handle initial root proto
        if (currentProto.getParentTestRecordId().isEmpty()) {
            handleRootProto(currentProto);
        } else if (currentProto.hasDescription()) {
            // If it has a Any Description with Context then it's a module
            handleModuleProto(currentProto);
        } else if (mCurrentRunName == null
                || currentProto.getTestRecordId().equals(mCurrentRunName)) {
            // Need to track the parent test run id to make sure we need testRunEnd or testRunFail
            handleTestRun(currentProto);
        } else {
            // Test cases handling
            handleTestCase(currentProto);
        }
    }

    @VisibleForTesting
    IInvocationContext getInvocationContext() {
        return mContext;
    }

    private void evalProto(List<ChildReference> children, boolean isInRun) {
        for (ChildReference child : children) {
            TestRecord childProto = child.getInlineTestRecord();
            if (isInRun) {
                // test case
                String[] info = childProto.getTestRecordId().split("#");
                TestDescription description = new TestDescription(info[0], info[1]);
                mListener.testStarted(description, timeStampToMillis(childProto.getStartTime()));
                handleTestCaseEnd(description, childProto);
            } else {
                boolean inRun = false;
                if (childProto.hasDescription()) {
                    // Module start
                    handleModuleStart(childProto);
                } else {
                    // run start
                    handleTestRunStart(childProto);
                    inRun = true;
                }
                evalProto(childProto.getChildrenList(), inRun);
                if (childProto.hasDescription()) {
                    // Module end
                    mListener.testModuleEnded();
                } else {
                    // run end
                    handleTestRunEnd(childProto);
                }
            }
        }
    }

    /** Handles the root of the invocation: They have no parent record id. */
    private void handleRootProto(TestRecord rootProto) {
        if (rootProto.hasEndTime()) {
            handleInvocationEnded(rootProto);
        } else {
            handleInvocationStart(rootProto);
        }
    }

    private void handleInvocationStart(TestRecord startInvocationProto) {
        // invocation starting
        Any anyDescription = startInvocationProto.getDescription();
        if (!anyDescription.is(Context.class)) {
            throw new RuntimeException("Expected Any description of type Context");
        }
        try {
            mContext = InvocationContext.fromProto(anyDescription.unpack(Context.class));
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }

        log("Received: invocation started proto");
        if (!mReportInvocation) {
            CLog.d("Skipping invocation start reporting.");
            return;
        }
        // Only report invocation start if enabled
        mListener.invocationStarted(mContext);
    }

    private void handleInvocationEnded(TestRecord endInvocationProto) {
        // Still report the logs even if not reporting the invocation level.
        handleLogs(endInvocationProto);

        // Get final context in case it changed.
        Any anyDescription = endInvocationProto.getDescription();
        if (!anyDescription.is(Context.class)) {
            throw new RuntimeException("Expected Any description of type Context");
        }
        try {
            IInvocationContext context =
                    InvocationContext.fromProto(anyDescription.unpack(Context.class));
            mergeInvocationContext(context);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
        log("Received: invocation ended proto");
        if (!mReportInvocation) {
            CLog.d("Skipping invocation ended reporting.");
            return;
        }
        // Only report invocation ended if enabled
        long elapsedTime =
                timeStampToMillis(endInvocationProto.getEndTime())
                        - timeStampToMillis(endInvocationProto.getStartTime());
        mListener.invocationEnded(elapsedTime);
    }

    /** Handles module level of the invocation: They have a Description for the module context. */
    private void handleModuleProto(TestRecord moduleProto) {
        if (moduleProto.hasEndTime()) {
            handleLogs(moduleProto);
            log("Received: test module ended proto");
            mListener.testModuleEnded();
        } else {
            handleModuleStart(moduleProto);
        }
    }

    private void handleModuleStart(TestRecord moduleProto) {
        Any anyDescription = moduleProto.getDescription();
        if (!anyDescription.is(Context.class)) {
            throw new RuntimeException("Expected Any description of type Context");
        }
        log("Received: test module started proto");
        try {
            IInvocationContext moduleContext =
                    InvocationContext.fromProto(anyDescription.unpack(Context.class));
            mListener.testModuleStarted(moduleContext);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    /** Handles the test run level of the invocation. */
    private void handleTestRun(TestRecord runProto) {
        // If the proto end-time is present we are evaluating the end of a test run.
        if (runProto.hasEndTime()) {
            handleTestRunEnd(runProto);
            mCurrentRunName = null;
        } else {
            // If the end-time is not populated yet we are dealing with the start of a run.
            mCurrentRunName = runProto.getTestRecordId();
            handleTestRunStart(runProto);
        }
    }

    private void handleTestRunStart(TestRecord runProto) {
        log("Received: test run started proto");
        if (runProto.getAttemptId() != 0) {
            mListener.testRunStarted(
                    runProto.getTestRecordId(),
                    (int) runProto.getNumExpectedChildren(),
                    (int) runProto.getAttemptId());
        } else {
            mListener.testRunStarted(
                    runProto.getTestRecordId(), (int) runProto.getNumExpectedChildren());
        }
    }

    private void handleTestRunEnd(TestRecord runProto) {
        // If we find debugging information, the test run failed and we reflect it.
        if (runProto.hasDebugInfo()) {
            mListener.testRunFailed(runProto.getDebugInfo().getErrorMessage());
        }
        handleLogs(runProto);
        log("Received: test run ended proto");
        long elapsedTime =
                timeStampToMillis(runProto.getEndTime())
                        - timeStampToMillis(runProto.getStartTime());
        HashMap<String, Metric> metrics = new HashMap<>(runProto.getMetrics());
        mListener.testRunEnded(elapsedTime, metrics);
    }

    /** Handles the test cases level of the invocation. */
    private void handleTestCase(TestRecord testcaseProto) {
        String[] info = testcaseProto.getTestRecordId().split("#");
        TestDescription description = new TestDescription(info[0], info[1]);
        if (testcaseProto.hasEndTime()) {
            handleTestCaseEnd(description, testcaseProto);
        } else {
            log("Received: test case started proto");
            mListener.testStarted(description, timeStampToMillis(testcaseProto.getStartTime()));
        }
    }

    private void handleTestCaseEnd(TestDescription description, TestRecord testcaseProto) {
        log("Received: test case ended proto");
        switch (testcaseProto.getStatus()) {
            case FAIL:
                mListener.testFailed(description, testcaseProto.getDebugInfo().getErrorMessage());
                break;
            case ASSUMPTION_FAILURE:
                mListener.testAssumptionFailure(
                        description, testcaseProto.getDebugInfo().getTrace());
                break;
            case IGNORED:
                mListener.testIgnored(description);
                break;
            case PASS:
                break;
            default:
                throw new RuntimeException(
                        String.format(
                                "Received unexpected test status %s.", testcaseProto.getStatus()));
        }
        handleLogs(testcaseProto);
        HashMap<String, Metric> metrics = new HashMap<>(testcaseProto.getMetrics());
        mListener.testEnded(description, timeStampToMillis(testcaseProto.getEndTime()), metrics);
    }

    private long timeStampToMillis(Timestamp stamp) {
        return stamp.getSeconds() * 1000L + (stamp.getNanos() / 1000000L);
    }

    private void handleLogs(TestRecord proto) {
        if (!(mListener instanceof ILogSaverListener)) {
            return;
        }
        ILogSaverListener logger = (ILogSaverListener) mListener;
        for (Entry<String, Any> entry : proto.getArtifacts().entrySet()) {
            try {
                LogFileInfo info = entry.getValue().unpack(LogFileInfo.class);
                LogFile file =
                        new LogFile(
                                info.getPath(),
                                info.getUrl(),
                                info.getIsCompressed(),
                                LogDataType.valueOf(info.getLogType()),
                                info.getSize());
                CLog.e("Logging %s from subprocess", entry.getKey());
                logger.logAssociation("subprocess-" + entry.getKey(), file);
            } catch (InvalidProtocolBufferException e) {
                CLog.e("Couldn't unpack %s as a LogFileInfo", entry.getKey());
                CLog.e(e);
            }
        }
    }

    private void mergeInvocationContext(IInvocationContext endInvocationContext) {
        if (mContext == null) {
            return;
        }
        // Gather attributes of build infos
        for (IBuildInfo info : mContext.getBuildInfos()) {
            String name = mContext.getBuildInfoName(info);
            IBuildInfo endInvocationInfo = endInvocationContext.getBuildInfo(name);
            if (endInvocationInfo == null) {
                continue;
            }
            info.addBuildAttributes(endInvocationInfo.getBuildAttributes());
        }
    }

    private void log(String format, Object... obj) {
        if (!mQuietParsing) {
            CLog.d(format, obj);
        }
    }
}

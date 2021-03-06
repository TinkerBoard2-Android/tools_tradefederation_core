/*
 * Copyright (C) 2010 The Android Open Source Project
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
package com.android.tradefed.result;

import com.android.tradefed.invoker.IInvocationContext;

import java.util.List;

/**
 * Interface that allows {@link ITestInvocationListener} to exchange some limited information via
 * summaries.
 */
public interface ITestSummaryListener extends ITestInvocationListener {

    /**
     * Passes a {@link List} of non-null {@link TestSummary}s which may have been returned from any
     * {@link ITestInvocationListener}s instantiated as part of the configuration. The early
     * summaries are generated after {@link #invocationStarted(IInvocationContext)} and can be
     * completed at the end of the invocation via {@link #putSummary(List)}.
     *
     * <p>This is called before {@link #invocationStarted(IInvocationContext)} and contains all the
     * summaries from the listeners configured before it.
     *
     * @param summaries A {@link List} of non-null {@link TestSummary}s from {@link
     *     ITestInvocationListener}s that are part of the current configuration.
     */
    public default void putEarlySummary(List<TestSummary> summaries) {
        // Empty default implementation
    }

    /**
     * Passes a {@link List} of non-null {@link TestSummary}s which may have been returned from any
     * {@link ITestInvocationListener}s instantiated as part of the configuration.
     *
     * @param summaries A {@link List} of non-null {@link TestSummary}s from {@link
     *     ITestInvocationListener}s that are part of the current configuration.
     */
    public default void putSummary(List<TestSummary> summaries) {
        // Empty default implementation
    }

}

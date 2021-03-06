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
package com.android.tradefed.result.suite;

import java.io.File;
import java.io.IOException;

/** Interface describing a formatter for {@link SuiteResultHolder}. */
public interface IFormatterGenerator {

    /**
     * Write the results into a particular format.
     *
     * @param holder The results held in {@link SuiteResultHolder}.
     * @param resultDir The directory where results should be stored.
     * @return a {@link File} containing the results in a formatted manner.
     */
    public File writeResults(SuiteResultHolder holder, File resultDir) throws IOException;

    /**
     * Reverse operation of {@link #writeResults(SuiteResultHolder, File)} which takes a result
     * directory and create the {@link SuiteResultHolder} out of it.
     *
     * @param resultDir The directory where to find the results.
     * @param shallow only load the top level information of {@link SuiteResultHolder}.
     * @return A {@link SuiteResultHolder} containing the results representation. Or null if
     *     anything goes wrong.
     */
    public SuiteResultHolder parseResults(File resultDir, boolean shallow) throws IOException;
}

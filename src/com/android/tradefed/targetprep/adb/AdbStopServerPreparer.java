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
package com.android.tradefed.targetprep.adb;

import com.android.tradefed.build.IBuildInfo;
import com.android.tradefed.config.GlobalConfiguration;
import com.android.tradefed.device.DeviceNotAvailableException;
import com.android.tradefed.device.IDeviceManager;
import com.android.tradefed.device.ITestDevice;
import com.android.tradefed.targetprep.BaseTargetPreparer;
import com.android.tradefed.targetprep.BuildError;
import com.android.tradefed.targetprep.ITargetCleaner;
import com.android.tradefed.targetprep.TargetSetupError;
import com.android.tradefed.util.IRunUtil;
import com.android.tradefed.util.RunUtil;

/** Target preparer to stop adb server on the host before and after running adb tests. */
public class AdbStopServerPreparer extends BaseTargetPreparer implements ITargetCleaner {

    private IRunUtil mRunUtil = RunUtil.getDefault();

    private static final long CMD_TIMEOUT = 60000L;

    /** {@inheritDoc} */
    @Override
    public void setUp(ITestDevice device, IBuildInfo buildInfo)
            throws TargetSetupError, BuildError, DeviceNotAvailableException {

        getDeviceManager().stopAdbBridge();

        // Kill the default adb server
        mRunUtil.runTimedCmd(CMD_TIMEOUT, "adb", "kill-server");
        // Wait 1000ms for "adb kill-server" to finish. See b/37104408
        mRunUtil.sleep(1 * 1000);
    }

    /** {@inheritDoc} */
    @Override
    public void tearDown(ITestDevice device, IBuildInfo buildInfo, Throwable e)
            throws DeviceNotAvailableException {
        // Kill the test adb server
        mRunUtil.runTimedCmd(CMD_TIMEOUT, "adb", "kill-server");
        // Wait 1000ms for "adb kill-server" to finish. See b/37104408
        mRunUtil.sleep(1 * 1000);
        // Restart the default adb server found in system PATH. Ensure adb server is already
        // running the next time TF executes a command over adb bridge. Otherwise adb bridge may
        // see a "Connection refused" exception.
        mRunUtil.runTimedCmd(CMD_TIMEOUT, "adb", "start-server");
        mRunUtil.sleep(1 * 1000);

        getDeviceManager().restartAdbBridge();
    }

    IDeviceManager getDeviceManager() {
        return GlobalConfiguration.getDeviceManagerInstance();
    }
}
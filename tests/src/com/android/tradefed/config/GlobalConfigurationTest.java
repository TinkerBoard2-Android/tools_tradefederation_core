/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.tradefed.config;

import com.android.tradefed.command.CommandScheduler;
import com.android.tradefed.config.Option.Importance;
import com.android.tradefed.util.ArrayUtil;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit Tests for {@link GlobalConfiguration}
 * It is difficult to test since GlobalConfiguration is a singleton and we cannot use reflection to
 * unset the instance since it might be in use in the currently running Trade Federation instance.
 */
public class GlobalConfigurationTest extends TestCase {

    GlobalConfiguration mGlobalConfig;
    private final static String OPTION_DESCRIPTION = "mandatory option should be set";
    private final static String OPTION_NAME = "manda";
    private final static String ALIAS_NAME = "aliasssss";
    private final static String EMPTY_CONFIG = "empty";
    private static final String GLOBAL_TEST_CONFIG = "global-config";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mGlobalConfig = new GlobalConfiguration(EMPTY_CONFIG, "description");
        assertNotNull(mGlobalConfig);
    }

    @OptionClass(alias = ALIAS_NAME)
    private class FakeCommandScheduler extends CommandScheduler {
        @Option(name = OPTION_NAME, description = OPTION_DESCRIPTION, mandatory = true,
                importance = Importance.IF_UNSET)
        private String mandatoryTest = null;
    }

    /**
     * Test for {@link GlobalConfiguration#validateOptions()}
     */
    public void testValidateOptions() throws Exception {
        mGlobalConfig.validateOptions();
        mGlobalConfig.setCommandScheduler(new FakeCommandScheduler());
        try {
            mGlobalConfig.validateOptions();
            fail("Should have thrown a configuration exception");
        } catch (ConfigurationException ce) {
            // expected
        }
    }

    /**
     * Test that the creation of Global configuration with basic default parameter is working
     */
    public void testCreateGlobalConfiguration_empty() throws Exception {
        String[] args = {};
        List<String> nonGlobalArgs = new ArrayList<String>(args.length);
        IConfigurationFactory configFactory = ConfigurationFactory.getInstance();
        // use the known "empty" config as a global config to avoid issues.
        String globalConfigPath = EMPTY_CONFIG;
        IGlobalConfiguration mGlobalConfig = configFactory.createGlobalConfigurationFromArgs(
                ArrayUtil.buildArray(new String[] {globalConfigPath}, args), nonGlobalArgs);
        assertTrue(nonGlobalArgs.size() == 0);
        assertNotNull(mGlobalConfig);
        mGlobalConfig.validateOptions();
    }

    /**
     * Printing is properly reading the properties
     */
    public void testPrintCommandUsage() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true);
        mGlobalConfig.printCommandUsage(true, ps);
        String expected = "'empty' configuration: description\n\n" +
                "Printing help for only the important options. " +
                "To see help for all options, use the --help-all flag\n\n";
        assertEquals(expected, baos.toString());

        mGlobalConfig.setCommandScheduler(new FakeCommandScheduler());
        baos = new ByteArrayOutputStream();
        ps = new PrintStream(baos, true);
        mGlobalConfig.printCommandUsage(true, ps);
        expected = String.format("'%s' command_scheduler options:\n" +
                "    --%s              %s", ALIAS_NAME, OPTION_NAME, OPTION_DESCRIPTION);
        assertTrue(baos.toString().contains(expected));
    }

    /**
     * Test that the creation of Global configuration with basic global configuration that is not
     * empty.
     */
    public void testCreateGlobalConfiguration_nonEmpty() throws Exception {
        // one global arg, one non-global arg
        String[] args = {"test-tag", "test"};
        List<String> nonGlobalArgs = new ArrayList<String>(args.length);
        // In order to find our test config, we provide our testconfigs/ folder. Otherwise only
        // the config/ folder is searched for configuration by default.
        IConfigurationFactory configFactory = new ConfigurationFactory() {
            @Override
            String getConfigPrefix() {
                return "testconfigs/";
            }
        };
        String globalConfigPath = GLOBAL_TEST_CONFIG;
        IGlobalConfiguration mGlobalConfig = configFactory.createGlobalConfigurationFromArgs(
                ArrayUtil.buildArray(new String[] {globalConfigPath}, args), nonGlobalArgs);
        assertNotNull(mGlobalConfig);
        assertNotNull(mGlobalConfig.getDeviceMonitors());
        assertNotNull(mGlobalConfig.getWtfHandler());
        mGlobalConfig.validateOptions();
        // Only --test-tag test remains, the global config name has been removed.
        assertTrue(nonGlobalArgs.size() == 2);
    }
}
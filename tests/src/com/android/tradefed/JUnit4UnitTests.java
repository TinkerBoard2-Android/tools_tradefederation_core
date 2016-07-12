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
package com.android.tradefed;

import com.android.tradefed.build.BuildInfoTest;
import com.android.tradefed.build.DeviceBuildDescriptorTest;
import com.android.tradefed.build.DeviceBuildInfoTest;
import com.android.tradefed.build.FileDownloadCacheTest;
import com.android.tradefed.build.KernelBuildInfoTest;
import com.android.tradefed.build.KernelDeviceBuildInfoTest;
import com.android.tradefed.build.OtaZipfileBuildProviderTest;
import com.android.tradefed.build.SdkBuildInfoTest;
import com.android.tradefed.command.CommandFileParserTest;
import com.android.tradefed.command.CommandFileWatcherTest;
import com.android.tradefed.command.CommandOptionsTest;
import com.android.tradefed.command.CommandSchedulerTest;
import com.android.tradefed.command.ConsoleTest;
import com.android.tradefed.command.VerifyTest;
import com.android.tradefed.command.remote.RemoteOperationTest;
import com.android.tradefed.config.ArgsOptionParserTest;
import com.android.tradefed.config.ConfigurationDefTest;
import com.android.tradefed.config.ConfigurationFactoryTest;
import com.android.tradefed.config.ConfigurationTest;
import com.android.tradefed.config.ConfigurationXmlParserTest;
import com.android.tradefed.config.GlobalConfigurationTest;
import com.android.tradefed.config.OptionCopierTest;
import com.android.tradefed.config.OptionSetterTest;
import com.android.tradefed.config.OptionUpdateRuleTest;
import com.android.tradefed.device.BackgroundDeviceActionTest;
import com.android.tradefed.device.CpuStatsCollectorTest;
import com.android.tradefed.device.DeviceManagerTest;
import com.android.tradefed.device.DeviceSelectionOptionsTest;
import com.android.tradefed.device.DeviceStateMonitorTest;
import com.android.tradefed.device.DeviceUtilStatsMonitorTest;
import com.android.tradefed.device.DumpsysPackageReceiverTest;
import com.android.tradefed.device.FastbootHelperTest;
import com.android.tradefed.device.ManagedDeviceListTest;
import com.android.tradefed.device.ManagedTestDeviceFactoryTest;
import com.android.tradefed.device.NativeDeviceTest;
import com.android.tradefed.device.ReconnectingRecoveryTest;
import com.android.tradefed.device.RemoteAndroidDeviceTest;
import com.android.tradefed.device.TestDeviceTest;
import com.android.tradefed.device.TopHelperTest;
import com.android.tradefed.device.WaitDeviceRecoveryTest;
import com.android.tradefed.device.WifiHelperTest;
import com.android.tradefed.invoker.TestInvocationTest;
import com.android.tradefed.log.FileLoggerTest;
import com.android.tradefed.log.LogRegistryTest;
import com.android.tradefed.log.TerribleFailureEmailHandlerTest;
import com.android.tradefed.result.BugreportCollectorTest;
import com.android.tradefed.result.CollectingTestListenerTest;
import com.android.tradefed.result.ConsoleResultReporterTest;
import com.android.tradefed.result.DeviceFileReporterTest;
import com.android.tradefed.result.EmailResultReporterTest;
import com.android.tradefed.result.FailureEmailResultReporterTest;
import com.android.tradefed.result.FileSystemLogSaverTest;
import com.android.tradefed.result.InvocationFailureEmailResultReporterTest;
import com.android.tradefed.result.InvocationToJUnitResultForwarderTest;
import com.android.tradefed.result.JUnitToInvocationResultForwarderTest;
import com.android.tradefed.result.LogFileSaverTest;
import com.android.tradefed.result.SnapshotInputStreamSourceTest;
import com.android.tradefed.result.TestFailureEmailResultReporterTest;
import com.android.tradefed.result.TestSummaryTest;
import com.android.tradefed.result.XmlResultReporterTest;
import com.android.tradefed.targetprep.AllTestAppsInstallSetupTest;
import com.android.tradefed.targetprep.BuildInfoAttributePreparerTest;
import com.android.tradefed.targetprep.DefaultTestsZipInstallerTest;
import com.android.tradefed.targetprep.DeviceFlashPreparerTest;
import com.android.tradefed.targetprep.DeviceSetupTest;
import com.android.tradefed.targetprep.FastbootDeviceFlasherTest;
import com.android.tradefed.targetprep.FlashingResourcesParserTest;
import com.android.tradefed.targetprep.InstrumentationPreparerTest;
import com.android.tradefed.targetprep.KernelFlashPreparerTest;
import com.android.tradefed.targetprep.PushFilePreparerTest;
import com.android.tradefed.targetprep.PythonVirtualenvPreparerTest;
import com.android.tradefed.targetprep.SdkAvdPreparerTest;
import com.android.tradefed.targetprep.StopServicesSetupTest;
import com.android.tradefed.targetprep.SystemUpdaterDeviceFlasherTest;
import com.android.tradefed.testtype.AndroidJUnitTestTest;
import com.android.tradefed.testtype.DeviceBatteryLevelCheckerTest;
import com.android.tradefed.testtype.DeviceTestCaseTest;
import com.android.tradefed.testtype.DeviceTestSuiteTest;
import com.android.tradefed.testtype.FakeTestTest;
import com.android.tradefed.testtype.GTestListTestParserTest;
import com.android.tradefed.testtype.GTestResultParserTest;
import com.android.tradefed.testtype.GTestTest;
import com.android.tradefed.testtype.GTestXmlResultParserTest;
import com.android.tradefed.testtype.GoogleBenchmarkResultParserTest;
import com.android.tradefed.testtype.GoogleBenchmarkTestTest;
import com.android.tradefed.testtype.HostTestTest;
import com.android.tradefed.testtype.InstalledInstrumentationsTestTest;
import com.android.tradefed.testtype.InstrumentationFileTestTest;
import com.android.tradefed.testtype.InstrumentationSerialTestTest;
import com.android.tradefed.testtype.InstrumentationTestTest;
import com.android.tradefed.testtype.NativeBenchmarkTestParserTest;
import com.android.tradefed.testtype.NativeStressTestParserTest;
import com.android.tradefed.testtype.NativeStressTestTest;
import com.android.tradefed.testtype.PythonUnitTestResultParserTest;
import com.android.tradefed.testtype.PythonUnitTestRunnerTest;
import com.android.tradefed.testtype.testdefs.XmlDefsParserTest;
import com.android.tradefed.testtype.testdefs.XmlDefsTestTest;
import com.android.tradefed.util.AaptParserTest;
import com.android.tradefed.util.AbiFormatterTest;
import com.android.tradefed.util.ArrayUtilTest;
import com.android.tradefed.util.BluetoothUtilsTest;
import com.android.tradefed.util.ByteArrayListTest;
import com.android.tradefed.util.ClassPathScannerTest;
import com.android.tradefed.util.ConditionPriorityBlockingQueueTest;
import com.android.tradefed.util.ConfigCompletorTest;
import com.android.tradefed.util.EmailTest;
import com.android.tradefed.util.FakeTestsZipFolderTest;
import com.android.tradefed.util.FileUtilTest;
import com.android.tradefed.util.FixedByteArrayOutputStreamTest;
import com.android.tradefed.util.JUnitXmlParserTest;
import com.android.tradefed.util.ListInstrumentationParserTest;
import com.android.tradefed.util.LogcatUpdaterEventParserTest;
import com.android.tradefed.util.MultiMapTest;
import com.android.tradefed.util.NullUtilTest;
import com.android.tradefed.util.PairTest;
import com.android.tradefed.util.PropertyChangerTest;
import com.android.tradefed.util.QuotationAwareTokenizerTest;
import com.android.tradefed.util.RegexTrieTest;
import com.android.tradefed.util.RunUtilTest;
import com.android.tradefed.util.SimpleStatsTest;
import com.android.tradefed.util.SizeLimitedOutputStreamTest;
import com.android.tradefed.util.StreamUtilTest;
import com.android.tradefed.util.StringEscapeUtilsTest;
import com.android.tradefed.util.SubprocessTestResultsParserTest;
import com.android.tradefed.util.TableFormatterTest;
import com.android.tradefed.util.TestLoaderTest;
import com.android.tradefed.util.TimeValTest;
import com.android.tradefed.util.ZipUtilTest;
import com.android.tradefed.util.keystore.JSONFileKeyStoreClientTest;
import com.android.tradefed.util.net.HttpHelperTest;
import com.android.tradefed.util.net.HttpMultipartPostTest;
import com.android.tradefed.util.net.XmlRpcHelperTest;
import com.android.tradefed.util.xml.AndroidManifestWriterTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * A test suite for all Trade Federation unit tests running under Junit4.
 * <p/>
 * All tests listed here should be self-contained, and should not require any external dependencies.
 */
@RunWith(Suite.class)
@SuiteClasses({

    // build
    BuildInfoTest.class,
    DeviceBuildInfoTest.class,
    DeviceBuildDescriptorTest.class,
    FileDownloadCacheTest.class,
    KernelBuildInfoTest.class,
    KernelDeviceBuildInfoTest.class,
    OtaZipfileBuildProviderTest.class,
    SdkBuildInfoTest.class,

    // command
    CommandFileParserTest.class,
    CommandFileWatcherTest.class,
    CommandSchedulerTest.class,
    CommandOptionsTest.class,
    ConsoleTest.class,
    VerifyTest.class,

    // command.remote
    RemoteOperationTest.class,

    // config
    ArgsOptionParserTest.class,
    ConfigurationDefTest.class,
    ConfigurationFactoryTest.class,
    ConfigurationTest.class,
    ConfigurationXmlParserTest.class,
    GlobalConfigurationTest.class,
    OptionCopierTest.class,
    OptionSetterTest.class,
    OptionUpdateRuleTest.class,

    // device
    BackgroundDeviceActionTest.class,
    CpuStatsCollectorTest.class,
    DeviceManagerTest.class,
    DeviceSelectionOptionsTest.class,
    DeviceStateMonitorTest.class,
    DeviceUtilStatsMonitorTest.class,
    DumpsysPackageReceiverTest.class,
    FastbootHelperTest.class,
    ManagedDeviceListTest.class,
    ManagedTestDeviceFactoryTest.class,
    NativeDeviceTest.class,
    ReconnectingRecoveryTest.class,
    RemoteAndroidDeviceTest.class,
    PropertyChangerTest.class,
    TestDeviceTest.class,
    TopHelperTest.class,
    WaitDeviceRecoveryTest.class,
    WifiHelperTest.class,

    // invoker
    TestInvocationTest.class,

    // log
    FileLoggerTest.class,
    LogRegistryTest.class,
    TerribleFailureEmailHandlerTest.class,

    // result
    BugreportCollectorTest.class,
    ConsoleResultReporterTest.class,
    CollectingTestListenerTest.class,
    DeviceFileReporterTest.class,
    EmailResultReporterTest.class,
    FailureEmailResultReporterTest.class,
    FileSystemLogSaverTest.class,
    InvocationFailureEmailResultReporterTest.class,
    InvocationToJUnitResultForwarderTest.class,
    JUnitToInvocationResultForwarderTest.class,
    LogFileSaverTest.class,
    SnapshotInputStreamSourceTest.class,
    TestSummaryTest.class,
    TestFailureEmailResultReporterTest.class,
    XmlResultReporterTest.class,

    // targetprep
    AllTestAppsInstallSetupTest.class,
    BuildInfoAttributePreparerTest.class,
    DefaultTestsZipInstallerTest.class,
    DeviceFlashPreparerTest.class,
    DeviceSetupTest.class,
    FastbootDeviceFlasherTest.class,
    FlashingResourcesParserTest.class,
    InstrumentationPreparerTest.class,
    KernelFlashPreparerTest.class,
    PushFilePreparerTest.class,
    PythonVirtualenvPreparerTest.class,
    SdkAvdPreparerTest.class,
    StopServicesSetupTest.class,
    SystemUpdaterDeviceFlasherTest.class,

    // testtype
    AndroidJUnitTestTest.class,
    DeviceBatteryLevelCheckerTest.class,
    DeviceTestCaseTest.class,
    DeviceTestSuiteTest.class,
    FakeTestTest.class,
    GoogleBenchmarkResultParserTest.class,
    GoogleBenchmarkTestTest.class,
    GTestListTestParserTest.class,
    GTestResultParserTest.class,
    GTestTest.class,
    GTestXmlResultParserTest.class,
    HostTestTest.class,
    InstalledInstrumentationsTestTest.class,
    InstrumentationSerialTestTest.class,
    InstrumentationFileTestTest.class,
    InstrumentationTestTest.class,
    NativeBenchmarkTestParserTest.class,
    NativeStressTestParserTest.class,
    NativeStressTestTest.class,
    PythonUnitTestResultParserTest.class,
    PythonUnitTestRunnerTest.class,

    // testtype/testdefs
    XmlDefsParserTest.class,
    XmlDefsTestTest.class,

    // util
    AaptParserTest.class,
    AbiFormatterTest.class,
    ArrayUtilTest.class,
    BluetoothUtilsTest.class,
    ByteArrayListTest.class,
    ClassPathScannerTest.class,
    ConditionPriorityBlockingQueueTest.class,
    ConfigCompletorTest.class,
    EmailTest.class,
    FakeTestsZipFolderTest.class,
    FileUtilTest.class,
    FixedByteArrayOutputStreamTest.class,
    HttpHelperTest.class,
    HttpMultipartPostTest.class,
    JUnitXmlParserTest.class,
    ListInstrumentationParserTest.class,
    LogcatUpdaterEventParserTest.class,
    MultiMapTest.class,
    NullUtilTest.class,
    PairTest.class,
    QuotationAwareTokenizerTest.class,
    RegexTrieTest.class,
    RunUtilTest.class,
    SizeLimitedOutputStreamTest.class,
    StreamUtilTest.class,
    SubprocessTestResultsParserTest.class,
    TableFormatterTest.class,
    TestLoaderTest.class,
    TimeValTest.class,
    SimpleStatsTest.class,
    StringEscapeUtilsTest.class,
    XmlRpcHelperTest.class,
    ZipUtilTest.class,

    // util subdirs
    AndroidManifestWriterTest.class,

    // util/keystore
    JSONFileKeyStoreClientTest.class,

})
public class JUnit4UnitTests {
    // empty of purpose
}

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

package com.android.tradefed.config.gcs;

import com.android.tradefed.config.ConfigurationDef;
import com.android.tradefed.config.ConfigurationException;
import com.android.tradefed.config.ConfigurationFactory;
import com.android.tradefed.config.IConfigurationFactory;
import com.android.tradefed.config.IConfigurationServer;
import com.android.tradefed.util.FileUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/** A {@link ConfigurationFactory} loads configs from Google Cloud Storage. */
public class GCSConfigurationFactory extends ConfigurationFactory {
    private static IConfigurationFactory sInstance = null;
    private IConfigurationServer mConfigServer;
    /** Keep track of the latest downloaded config file so we can use it */
    private File mConfigDownloaded = null;

    GCSConfigurationFactory(IConfigurationServer configServer) {
        mConfigServer = configServer;
    }

    /** Get the singleton {@link IConfigurationFactory} instance. */
    public static IConfigurationFactory getInstance(IConfigurationServer configServer) {
        if (sInstance == null) {
            sInstance = new GCSConfigurationFactory(configServer);
        }
        return sInstance;
    }

    /**
     * Loads an InputStream for given config name from Google Cloud Storage(GCS).
     *
     * @param name the configuration name to load
     * @return a {@link BufferedInputStream} for reading config contents
     * @throws ConfigurationException if config could not be found
     */
    @Override
    protected BufferedInputStream getConfigStream(String name) throws ConfigurationException {
        InputStream configStream = getBundledConfigStream(name);
        if (configStream == null) {
            // now try to load from GCS
            configStream = mConfigServer.getConfig(name);
            if (configStream == null) {
                throw new ConfigurationException(
                        String.format("Could not find configuration '%s'", name));
            }
            // Create a local copy of the configuration
            try {
                // If available, store the downloaded global config within the Tradefed directory
                File tfDir = null;
                String tfPath = System.getProperty("TF_JAR_DIR");
                if (tfPath != null) {
                    // In some cases, a '.' for current is given, handle it.
                    if (tfPath.equals(".")) {
                        tfDir = new File("").getAbsoluteFile();
                    } else {
                        tfDir = new File(tfPath).getAbsoluteFile();
                    }
                }
                mConfigDownloaded =
                        FileUtil.createTempFile("gcs-downloaded-global-config", ".xml", tfDir);
                mConfigDownloaded.deleteOnExit();
                FileUtil.writeToFile(configStream, mConfigDownloaded);
                // Reset the stream to be available from the start again.
                configStream.reset();
            } catch (IOException e) {
                throw new ConfigurationException(e.getMessage());
            }
        }
        // buffer input for performance - just in case config file is large
        return new BufferedInputStream(configStream);
    }

    public File getLatestDownloadedFile() {
        return mConfigDownloaded;
    }

    /** {@inheritDoc} */
    @Override
    protected ConfigurationDef getConfigurationDef(
            String name, boolean isGlobal, Map<String, String> templateMap)
            throws ConfigurationException {
        return new GCSConfigLoader(isGlobal).getConfigurationDef(name, templateMap);
    }

    /**
     * Extension of {@link com.android.tradefed.config.ConfigurationFactory.ConfigLoader} that loads
     * config from GCS, tracks the included configurations from one root config, and throws an
     * exception on circular includes.
     */
    protected class GCSConfigLoader extends ConfigLoader {

        public GCSConfigLoader(boolean isGlobalConfig) {
            super(isGlobalConfig);
        }

        @Override
        protected String findConfigName(String name, String parentName)
                throws ConfigurationException {
            if (isBundledConfig(name)) {
                return name;
            }
            if (parentName == null || isBundledConfig(parentName)) {
                return name;
            }

            return getPath(parentName, name);
        }

        /**
         * Help method to get file's path from its parent and filename. This is mainly for normalize
         * path like path/to/../file to path/file
         *
         * @param parent parent name
         * @param filename file name
         * @return normalized path
         */
        String getPath(String parent, String filename) {
            Path parentPath = Paths.get(parent).getParent();
            if (parentPath == null) {
                return filename;
            }
            return parentPath.resolve(filename).normalize().toString();
        }

        /** {@inheritDoc} */
        @Override
        protected void trackConfig(String name, ConfigurationDef def) {
            // FIXME: Track remote config source files' life cycle.
            return;
        }
    }
}

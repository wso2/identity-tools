/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.is.configuration.diff.creater;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.is.configuration.diff.creater.exception.ConfigMigrateException;
import org.wso2.is.configuration.diff.creater.utils.MigrationConstants;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The class which handles writing to the output files.
 */
public class OutputGenerator {

    private static final Log log = LogFactory.getLog(OutputGenerator.class);
    private File outputCSV = new File(MigrationConstants.OUTPUT_FOLDER.concat(MigrationConstants.FILE_SEPARATOR)
            .concat(MigrationConstants.OUTPUT_CATALOG_CSV));
    private File outputDirectory = new File(MigrationConstants.OUTPUT_FOLDER);
    private File unTemplatedFileDirectory = new File(MigrationConstants.UN_TEMPLATE_FILE_FOLDER);
    private File logFile = new File(MigrationConstants.LOG_FILE);
    private Map<String, String> keyValuesMap = new HashMap<>();
    private Map<String, String> keyCatalogValuesMap = new HashMap<>();

    /**
     * Create all the output csv files and directory.
     *
     * @throws ConfigMigrateException ConfigMigrateException.
     */
    public void createOutputFiles() throws ConfigMigrateException {

        try {
            createDirectory(outputDirectory);
            createDirectory(unTemplatedFileDirectory);

            createFile(outputCSV);
            setOutputCSV(outputCSV);

            createFile(logFile);
            setLogFile(logFile);
        } catch (IOException e) {
            throw new ConfigMigrateException("Error occurred when creating output files.");
        }
    }

    /**
     * Create a directory to put un-template customized config files in the migrated setup.
     *
     * @param outputDirectory create directory.
     * @throws IOException exception when director is not created.
     */
    private void createDirectory(File outputDirectory) throws IOException {

        if (outputDirectory.exists()) {
            FileUtils.deleteDirectory(outputDirectory);
        }
        if (!outputDirectory.mkdir()) {
            log.error("The output directory, " + outputDirectory.getName() + " is not created.");
            throw new IOException("The output directory, " + outputDirectory.getName() + " is not created.");
        }
        setOutputDirectory(outputDirectory);
    }

    /**
     * Create new file after removing the existing one.
     *
     * @param file file
     * @throws IOException Exception is thrown if file is not deleted and created.
     */
    private void createFile(File file) throws IOException {

        if (file.exists() && !file.delete()) {
            log.error("The output file, " + file.getName() + " is not deleted.");
            throw new IOException("The output file, " + file.getName() + " is not deleted.");
        }
        if (!file.createNewFile()) {
            log.error("The output file, " + file.getName() + " is not created.");
            throw new IOException("The output file, " + file.getName() + " is not created.");
        }
    }

    public File getOutputCSV() {

        return outputCSV;
    }

    private void setOutputCSV(File outputCSV) {

        this.outputCSV = outputCSV;
    }

    private void setOutputDirectory(File outputDirectory) {

        this.outputDirectory = outputDirectory;
    }

    public File getLogFile() {

        return logFile;
    }

    public void setLogFile(File logFile) {

        this.logFile = logFile;
    }

    public Map<String, String> getKeyValuesMap() {

        return keyValuesMap;
    }

    public void setKeyValuesMap(Map<String, String> keyValuesMap) {

        this.keyValuesMap = keyValuesMap;
    }

    public Map<String, String> getKeyCatalogValuesMap() {

        return keyCatalogValuesMap;
    }

    public void setKeyCatalogValuesMap(Map<String, String> keyCatalogValuesMap) {

        this.keyCatalogValuesMap = keyCatalogValuesMap;
    }
}

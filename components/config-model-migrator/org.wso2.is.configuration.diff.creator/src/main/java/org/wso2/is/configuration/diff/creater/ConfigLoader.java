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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.is.configuration.diff.creater.exception.ConfigMigrateException;
import org.wso2.is.configuration.diff.creater.utils.MigrationConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Class for loading config files.
 */
public class ConfigLoader {

    private Map<String, File> defaultXMLFiles = new HashMap<>();
    private Map<String, File> migratedXMLFiles = new HashMap<>();
    private Map<String, File> defaultPropertiesFiles = new HashMap<>();
    private Map<String, File> migratedPropertiesFiles = new HashMap<>();
    private Map<String, File> j2TemplateFiles = new HashMap<>();
    private static final int TAG_KEY_COLUMN_NO_INDEX = 2;

    private static final Logger log = LogManager.getLogger(ConfigLoader.class);

    /**
     * Validate whether file path is blank and exists.
     *
     * @param migratedISHomePath Migrated IS home path.
     * @param defaultISHomePath  Default IS home path.
     */
    public void validate(String migratedISHomePath, String defaultISHomePath) throws ConfigMigrateException {

        String errorMsg;
        if (StringUtils.isBlank(migratedISHomePath) && StringUtils.isBlank(defaultISHomePath)) {
             errorMsg = "Please enter migrated IS home path and default IS home path as inputs.";
            log.error(errorMsg);
            throw new ConfigMigrateException(errorMsg);
        }
        if (!new File(migratedISHomePath).exists() && !new File(defaultISHomePath).exists()) {
            errorMsg = "The provided IS home paths does not exist. Please try again after entering correct input" +
                    " Paths.";
            log.error(errorMsg);
            throw new ConfigMigrateException(errorMsg);
        }
    }

    /**
     * Filter files by the extension and add to maps.
     *
     * @param migratedProductHomePath migrated IS home path.
     * @param defaultProductHomePath  default IS home path.
     * @throws ConfigMigrateException ConfigMigrateException.
     */
    public void filterFiles(String migratedProductHomePath, String defaultProductHomePath)
            throws ConfigMigrateException {

        try {
            defaultXMLFiles = getFilesInPath(Paths.get(defaultProductHomePath.concat(MigrationConstants.CONF_PATH)),
                    MigrationConstants.XML_FILE_EXTENSION);
            migratedXMLFiles = getFilesInPath(Paths.get(migratedProductHomePath.concat(MigrationConstants.CONF_PATH)),
                    MigrationConstants.XML_FILE_EXTENSION);

            defaultPropertiesFiles = getFilesInPath(Paths.get(defaultProductHomePath.concat(MigrationConstants
                    .CONF_PATH)), MigrationConstants.PROPERTY_FILE_EXTENSION);
            migratedPropertiesFiles = getFilesInPath(Paths.get(migratedProductHomePath.concat(MigrationConstants
                    .CONF_PATH)), MigrationConstants.PROPERTY_FILE_EXTENSION);

            j2TemplateFiles = getFilesInPath(Paths.get(defaultProductHomePath.concat(MigrationConstants
                    .TEMPLATE_CONF_PATH)), MigrationConstants.J2_FILE_EXTENSION);
            log.info("Files have been filtered successfully!");

        } catch (IOException e) {
            throw new ConfigMigrateException("Error occurred when filtering files by extension type.", e);
        }
    }

    /**
     * Read the knowledge from hosted  pipe separated CSV file and put that to a Map.
     *
     * @param csvRemoteURL Remotely hosted CSV file URL.
     * @return Map of key and entry.
     * @throws IOException IOException.
     */
    public Map<String, String> readFromCSV(URL csvRemoteURL) throws IOException {

        String line;
        Map<String, String> keys = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvRemoteURL.openStream(),
                StandardCharsets.UTF_8))) {
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(Pattern.quote(MigrationConstants.CSV_SEPARATOR_APPENDER));
                if (isNewLine(parts)) {
                    keys.put(parts[TAG_KEY_COLUMN_NO_INDEX].trim(), line);
                }
            }
        }
        return keys;
    }

    private boolean isNewLine(String[] parts) {

        return parts.length > TAG_KEY_COLUMN_NO_INDEX;
    }

    /**
     * Traverse through the files in the folder and group files using the file extension.
     *
     * @param defaultPath   Path to the config folder.
     * @param fileExtension File extension type to be separated.
     * @return Map of file name and File.
     * @throws IOException IOException
     */
    private Map<String, File> getFilesInPath(Path defaultPath, String fileExtension) throws IOException {

        Map<String, File> mapOfFiles = new HashMap<>();
        List<String> fileList = Files.walk(defaultPath).map(Path::toString)
                .filter(f -> f.endsWith(fileExtension)).collect(Collectors.toList());
        for (String path : fileList) {
            File file = new File(path);
            mapOfFiles.put(file.getName(), file);
        }
        return mapOfFiles;
    }

    public Map<String, File> getDefaultXMLFiles() {

        return defaultXMLFiles;
    }

    public Map<String, File> getMigratedXMLFiles() {

        return migratedXMLFiles;
    }

    public Map<String, File> getDefaultPropertiesFiles() {

        return defaultPropertiesFiles;
    }

    public Map<String, File> getMigratedPropertiesFiles() {

        return migratedPropertiesFiles;
    }

    public Map<String, File> getJ2TemplateFiles() {

        return j2TemplateFiles;
    }
}

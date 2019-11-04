/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.toml.generator.util;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.configuration.diff.creater.exception.ConfigMigrateException;
import org.wso2.configuration.diff.creater.utils.MigrationConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The class which contains methods needed for process of toml generation.
 */
public class TomlGenerator {

    private static final Log log = LogFactory.getLog(TomlGenerator.class);

    /**
     * Write the configuration map to a file.
     *
     * @param tomlKeyValueMap map of configuration.
     */
    public static void writeToTOMLFile(Map<String, Object> tomlKeyValueMap) {
        TomlWriter writer = new TomlWriter();
        try {
            File outputDeploymentFile = getFile();
            if (outputDeploymentFile.createNewFile()) {
                writer.write(tomlKeyValueMap, outputDeploymentFile);
            }
        } catch (IOException e) {
            log.error("error saving configuration", e);
        }
    }

    private static File getFile() {
        File outputDeploymentFile = new File(TomlGeneratorConstants.UPDATED_DEPLOYMENT_TOML);
        if (outputDeploymentFile.exists()) {
            if (!outputDeploymentFile.delete()) {
                log.error("The output Toml file can not be deleted!.");
            }
        }
        return outputDeploymentFile;
    }

    /**
     * Get toml key value map to write the toml file using CSV files as inputs.
     * @param outputCSVFile catalog csv file.
     * @param keyValueCSVFile key Value csv file.
     * @param deploymentTomlFile existing deployment toml file.
     * @return output as a map format to write to a toml file.
     * @throws IOException IOException.
     * @throws ConfigMigrateException ConfigMigrateException.
     */
    public Map<String, Object> getTomlKeyValueMap(File outputCSVFile,
                                                  File keyValueCSVFile,
                                                  File deploymentTomlFile) throws IOException, ConfigMigrateException {

        Map<String, Object> tomlKeyValueMap;
        Toml toml = new Toml();
        if (isFilesExists(outputCSVFile, keyValueCSVFile, deploymentTomlFile)) {

            Map<String, Object> generatedTomlKeyValueMap = generateTomlKeyMapFromData(outputCSVFile, keyValueCSVFile);
            Map<String, Object> existingTomlMap = toml.read(deploymentTomlFile).toMap();
            tomlKeyValueMap = concatTomlMaps(existingTomlMap, generatedTomlKeyValueMap);

        } else {
            throw new ConfigMigrateException("Please enter correct output CSV file path, key value CSV file path and" +
                    " deployment.tml path and try again.");
        }

        return tomlKeyValueMap;
    }

    /**
     * Concat and get the common configurations to one map.
     *
     * @param existingTomlMap existing toml map of IS.
     * @param generatedTomlKeyValueMap generated toml map from custom changes.
     * @return filtered map of required configurations to add to toml file.
     */
    private Map<String, Object> concatTomlMaps(Map<String, Object> existingTomlMap,
                                               Map<String, Object> generatedTomlKeyValueMap) {

        for (Map.Entry<String, Object> tomlEntry : existingTomlMap.entrySet()) {

            if (generatedTomlKeyValueMap.get(tomlEntry.getKey()) == null) {

                generatedTomlKeyValueMap.put(tomlEntry.getKey(), tomlEntry.getValue());
            } else {
                for (Map.Entry<String, String> propertyEntry : ((Map<String, String>) tomlEntry.getValue())
                        .entrySet()) {

                    ((Map<String, String>) generatedTomlKeyValueMap.get(tomlEntry.getKey())).
                            computeIfAbsent(propertyEntry.getKey(), k -> propertyEntry.getValue());
                }
            }
        }

        return generatedTomlKeyValueMap;
    }

    private boolean isFilesExists(File outputCSVFile, File keyValueCSVFile, File deploymentTomlFile) {

        return outputCSVFile.exists() && keyValueCSVFile.exists() && deploymentTomlFile.exists();
    }

    /**
     * Generate toml key value map from input CSV files.
     *
     * @param catalogCSVFile catalog csv file.
     * @param keyValueCSVFile key-value csv file.
     * @return map of toml key value objects.
     * @throws IOException IOException.
     */
    private Map<String, Object> generateTomlKeyMapFromData(File catalogCSVFile, File keyValueCSVFile)
            throws IOException {

        Map<String, String> keyTomlMap;
        Map<String, String> keyValueMap;
        keyTomlMap = readOutputCSV(catalogCSVFile, MigrationConstants.CATALOG);
        keyValueMap = readOutputCSV(keyValueCSVFile, MigrationConstants.KEY_VALUES);

        Map<String, Object> outputMap = new HashMap<>();

        for (Map.Entry<String, String> entry : keyTomlMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (StringUtils.isBlank(value)) {
                log.info("A missing toml config found for key : " + key + " and changed value : "
                        + keyValueMap.get(key));
                continue;
            }
            addKeyToOutputMap(keyValueMap, outputMap, key, value);
        }
        return outputMap;
    }

    private void addKeyToOutputMap(Map<String, String> keyValueMap, Map<String, Object> outputMap, String key,
                                   String value) {

        Map<String, String> propertyMap = new HashMap<>();
        int lastIndexOf = value.lastIndexOf(".");
        if (lastIndexOf > 0) {
            String property = value.substring(0, lastIndexOf);
            String tomlMainKey = value.substring(lastIndexOf + 1);
            propertyMap.put(property, keyValueMap.get(key));
            outputMap.put(tomlMainKey, propertyMap);
        }
    }

    private Map<String, String> readOutputCSV(File csvFile, String type) throws IOException {

        BufferedReader reader = null;
        Map<String, String> keys = new HashMap<>();
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile.getPath()),
                    StandardCharsets.UTF_8));
            switch (type) {
                case MigrationConstants.CATALOG:
                    readFileAndPutToMap(reader, keys, 3);
                    break;

                case MigrationConstants.KEY_VALUES:
                    readFileAndPutToMap(reader, keys, 1);
                    break;

                default:
                    break;
            }

        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return keys;
    }

    private void readFileAndPutToMap(BufferedReader reader, Map<String, String> keys, int columnIndex)
            throws IOException {

        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(Pattern.quote(MigrationConstants.CSV_SEPARATOR_APPENDER));
            if (parts.length > columnIndex) {
                keys.put(parts[columnIndex - 1].trim(), parts[columnIndex].trim());
            }
        }
    }

}

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

package org.wso2.is.configuration.toml.generator.util;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
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
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The class which contains methods needed for process of toml generation.
 */
public class TomlGenerator {

    private static final Logger log = LogManager.getLogger(TomlGenerator.class);

    /**
     * Write the configuration map to a file.
     *
     * @param tomlKeyValueMap map of configuration.
     */
    public void writeToTOMLFile(Map<String, Object> tomlKeyValueMap) {
        TomlWriter writer = new TomlWriter();
        try {
            File outputDeploymentFile = getOutputTomlFile();
            if (outputDeploymentFile.createNewFile()) {
                writer.write(tomlKeyValueMap, outputDeploymentFile);
            }
        } catch (IOException e) {
            log.error("error saving configuration", e);
        }
    }

    private File getOutputTomlFile() {
        File outputDeploymentFile = new File(TomlGeneratorConstants.UPDATED_DEPLOYMENT_TOML);
        if (outputDeploymentFile.exists()) {
            if (!outputDeploymentFile.delete()) {
                log.error("The output Toml file can not be deleted!.");
            }
        }
        return outputDeploymentFile;
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

        for (Map.Entry<String, Object> tomlEntry : generatedTomlKeyValueMap.entrySet()) {

            if (existingTomlMap.get(tomlEntry.getKey()) == null) {
                existingTomlMap.put(tomlEntry.getKey(), tomlEntry.getValue());
            } else {
                for (Map.Entry<String, String> propertyEntry : ((Map<String, String>) tomlEntry.getValue())
                        .entrySet()) {
                    ((Map<String, String>) existingTomlMap.get(tomlEntry.getKey())).put(propertyEntry.getKey(),
                            propertyEntry.getValue());
                }
            }
        }

        return existingTomlMap;
    }

    private boolean isFilesExists(File deploymentTomlFile) {

        return  deploymentTomlFile.exists();
    }

    /**
     * Generate toml key Map from knowledge.
     * @param keyValueMap
     * @return
     * @throws IOException
     */
    private Map<String, Object> generateTomlKeyMapFromData(Map<String, String> keyValueMap, File log)
            throws IOException {

        Map<String, String> keyTomlMap = readOutputCSV(new URL(MigrationConstants.CATALOG_URL));
        Map<String, Object> outputMap = new HashMap<>();

        for (Map.Entry<String, String> entry : keyValueMap.entrySet()) {
            String key = entry.getKey();
            String tomlKey = keyTomlMap.get(key);
            if (StringUtils.isBlank(tomlKey)) {
                Files.write(Paths.get(log.getPath()), ("A missing toml config found for key : " + key
                        + " and changed value : " + keyValueMap.get(key)).getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.APPEND);
                continue;
            }
            addKeyToOutputMap(keyValueMap,  key, tomlKey, outputMap);
        }
        return outputMap;
    }

    private void addKeyToOutputMap(Map<String, String> keyValueMap, String key, String tomlKey,
                                   Map<String, Object> outputMap) {

        int lastIndexOf = tomlKey.lastIndexOf(".");
        if (lastIndexOf > 0) {
            String tomlMainKey = tomlKey.substring(0, lastIndexOf);
            String property = tomlKey.substring(lastIndexOf + 1);
            if (outputMap.get(tomlMainKey) == null) {
                Map<String, String> propertyMap = new HashMap<>();
                propertyMap.put(property, keyValueMap.get(key));
                outputMap.put(tomlMainKey, propertyMap);
            } else {
                Map<String, String> propertyMap = (Map<String, String>) outputMap.get(tomlMainKey);
                propertyMap.put(property, keyValueMap.get(key));
                outputMap.replace(tomlMainKey, propertyMap);
            }

        }
    }

    private Map<String, String> readOutputCSV(URL csvRemoteURL) throws IOException {

        BufferedReader reader = null;
        Map<String, String> keys = new HashMap<>();
        try {
            reader = new BufferedReader(new InputStreamReader(csvRemoteURL.openStream(),
                    StandardCharsets.UTF_8));
            readFileAndPutToMap(reader, keys);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return keys;
    }

    private void readFileAndPutToMap(BufferedReader reader, Map<String, String> keys)
            throws IOException {

        String line;
        reader.readLine();
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(Pattern.quote(MigrationConstants.CSV_SEPARATOR_APPENDER));
            if (parts.length > 3) {
                keys.put(parts[3 - 1].trim(), parts[3].trim());
            }
        }
    }

    public Map<String, Object> generateTomlKeyValueMap(Map<String, String> keyValuesMapFromDiff,
                                                       File deploymentTomlFile,
                                                       File log) throws ConfigMigrateException {

        Map<String, Object> tomlKeyValueMap;
        Toml toml = new Toml();
        if (isFilesExists(deploymentTomlFile)) {

            Map<String, Object> generatedTomlKeyValueMap;
            try {
                generatedTomlKeyValueMap = generateTomlKeyMapFromData(keyValuesMapFromDiff, log);
            } catch (IOException e) {
                throw new ConfigMigrateException("Error occurred when generating deployment.toml file.", e);
            }
            Map<String, Object> existingTomlMap = toml.read(deploymentTomlFile).toMap();
            tomlKeyValueMap = concatTomlMaps(existingTomlMap, generatedTomlKeyValueMap);

        } else {
            throw new ConfigMigrateException("Please enter correct output CSV file path, key value CSV file path and" +
                    " deployment.tml path and try again.");
        }

        return tomlKeyValueMap;
    }

}

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
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.wso2.is.configuration.diff.creater.exception.ConfigMigrateException;
import org.wso2.is.configuration.diff.creater.utils.MigrationConstants;
import org.xmlunit.XMLUnitException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;
import org.xmlunit.diff.ElementSelectors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Main Class for migration configs.
 */
public class ConfigurationDiffChecker {

    private static final Logger log = LogManager.getLogger(ConfigurationDiffChecker.class);

    /**
     * Find diff  between two XML and Properties files and write to output CSV files.
     *
     * @param configLoader    ConfigLoader which loads all the files in conf folder and filter them.
     * @param outputGenerator OutputGenerator which writes output to the CSV files.
     * @throws ConfigMigrateException ConfigMigrateException
     */
    public void findConfigDiff(ConfigLoader configLoader, OutputGenerator outputGenerator)
            throws ConfigMigrateException {

        Map<String, String> existingTags;
        Map<String, String> keyValueMap = new LinkedHashMap<>();
        if (configLoader == null) {
            throw new ConfigMigrateException("Error occurred when loading files.");
        }
        try {
            existingTags = configLoader.readFromCSV(new URL(MigrationConstants.CATALOG_URL));
        } catch (IOException e) {
            throw new ConfigMigrateException("Malformed URL :" + MigrationConstants.CATALOG_URL, e);
        }
        checkXMLDiff(configLoader.getDefaultXMLFiles(), configLoader.getMigratedXMLFiles(),
                configLoader.getJ2TemplateFiles(), existingTags, keyValueMap, outputGenerator);

        checkPropertyDiff(configLoader.getDefaultPropertiesFiles(), configLoader.getMigratedPropertiesFiles(),
                configLoader.getJ2TemplateFiles(), existingTags, keyValueMap, outputGenerator);

        outputGenerator.setKeyCatalogValuesMap(existingTags);
        outputGenerator.setKeyValuesMap(keyValueMap);
    }

    /**
     * Check whether property files are templated or not, if true find diff.
     *
     * @param defaultPropertiesFiles  Map of Default pack's property files.
     * @param migratedPropertiesFiles Map of Migrated pack's property file.
     * @param j2TemplateFiles         Map of j2 template file set.
     * @param existingTags            Map of existing tags.
     * @param keyValues               Map of key values.
     * @param outputGenerator         OutputGenerator object.
     * @throws ConfigMigrateException ConfigMigrateException
     */
    private void checkPropertyDiff(Map<String, File> defaultPropertiesFiles,
                                   Map<String, File> migratedPropertiesFiles,
                                   Map<String, File> j2TemplateFiles,
                                   Map<String, String> existingTags,
                                   Map<String, String> keyValues,
                                   OutputGenerator outputGenerator) throws ConfigMigrateException {

        File logFile = outputGenerator.getLogFile();
        if (migratedPropertiesFiles == null) {
            throw new ConfigMigrateException("There are no property files to be migrated. ");
        }
        for (Map.Entry<String, File> entry : migratedPropertiesFiles.entrySet()) {
            try {
                if (isFileTemplated(defaultPropertiesFiles, j2TemplateFiles, entry)) {
                    Set<Map.Entry<String, String>> changedPropertyDiffSet =
                            findDiffPropertiesFiles(defaultPropertiesFiles.get(entry.getKey()),
                                    migratedPropertiesFiles.get(entry.getKey()));
                    getPropertyDiffToMaps(migratedPropertiesFiles.get(entry.getKey()), changedPropertyDiffSet,
                            existingTags, keyValues, outputGenerator);
                } else {
                    filterUnTemplatedFiles(defaultPropertiesFiles, migratedPropertiesFiles, logFile, entry);
                }
            } catch (IOException e) {
                throw new ConfigMigrateException("Error occurred when writing diff to the csv.", e);
            }
        }
    }

    /**
     * Find the difference of property files.
     *
     * @param defaultFile  default file to compare with.
     * @param migratedFile migrated file used to compare.
     * @return A set of difference.
     * @throws ConfigMigrateException ConfigMigrateException.
     */
    private Set<Map.Entry<String, String>> findDiffPropertiesFiles(File defaultFile, File migratedFile)
            throws ConfigMigrateException {

        Properties defaultProperties = new Properties();
        Properties migratedProperties = new Properties();
        try (FileInputStream defaultInputStream = new FileInputStream(defaultFile);
             FileInputStream migratedInputStream = new FileInputStream(migratedFile)) {
            defaultProperties.load(defaultInputStream);
            migratedProperties.load(migratedInputStream);
            Map<String, String> defaultMap = new HashMap<>((Map) defaultProperties);
            Map<String, String> migratedMap = new HashMap<>((Map) migratedProperties);

            Set<Map.Entry<String, String>> defaultPropertySet = defaultMap.entrySet();
            Set<Map.Entry<String, String>> migratedPropertySet = migratedMap.entrySet();
            // The migrated properties which are not available in the default properties.
            migratedPropertySet.removeAll(defaultPropertySet);
            return migratedPropertySet;
        } catch (IOException e) {
            throw new ConfigMigrateException("The configurations can not be loaded properly. Please try again. ", e);
        }
    }

    /**
     * Write difference to csv files.
     *
     * @param migratedFile       migrated file.
     * @param changedPropertySet property difference.
     * @param existingTags       existing tags in the knowledge base.
     * @param keyValues          key-value map.
     * @param outputGenerator    OutputGenerator object class.
     */
    private void getPropertyDiffToMaps(File migratedFile,
                                       Set<Map.Entry<String, String>> changedPropertySet,
                                       Map<String, String> existingTags,
                                       Map<String, String> keyValues,
                                       OutputGenerator outputGenerator) {

        for (Map.Entry<String, String> property : changedPropertySet) {
            String csvEntry;
            if (existingTags.get(property.getKey()) == null) {
                csvEntry = migratedFile.getName().concat(MigrationConstants.CSV_SEPARATOR_APPENDER).concat(
                        MigrationConstants.PROPERTIES_FILE_TYPE).concat(MigrationConstants.CSV_SEPARATOR_APPENDER)
                        .concat(property.getKey()).concat("| | | | | |");
                existingTags.put(property.getKey(), csvEntry);
                keyValues.put(property.getKey(), property.getValue());
                outputGenerator.setGenerateToml(false);
            } else {
                keyValues.put(property.getKey(), property.getValue());
            }
        }
    }

    private void checkXMLDiff(Map<String, File> defaultXMLFiles,
                              Map<String, File> migratedXMLFiles,
                              Map<String, File> j2files,
                              Map<String, String> existingTags,
                              Map<String, String> keyValueMap,
                              OutputGenerator outputGenerator) throws ConfigMigrateException {

        File logFile = outputGenerator.getLogFile();
        for (Map.Entry<String, File> entry : migratedXMLFiles.entrySet()) {
            try {
                if (isFileTemplated(defaultXMLFiles, j2files, entry)) {

                    Diff detailedDiff = compareXMLFiles(defaultXMLFiles.get(entry.getKey()),
                            migratedXMLFiles.get(entry.getKey()));
                    getDifferenceToMaps(migratedXMLFiles.get(entry.getKey()), detailedDiff, existingTags, keyValueMap
                            , outputGenerator);
                } else {
                    filterUnTemplatedFiles(defaultXMLFiles, migratedXMLFiles, logFile, entry);
                }
            } catch (IOException e) {
                log.error("Error occurred when parsing xml files or finding diff of xml files.");
                throw new ConfigMigrateException("Error occurred when parsing xml files or finding diff of xml files" +
                        ". ", e);
            }
        }
    }

    /**
     * Check whether there is a file matched to the give file in .j2 file list. Because that tells that this file is
     * templated or not.
     *
     * @param defaultXMLFiles Map of default config file list.
     * @param j2files         Map of j2 file list.
     * @param entry           Map entry of migrated config file list.
     * @return true or false, if exists.
     */
    private boolean isFileTemplated(Map<String, File> defaultXMLFiles, Map<String, File> j2files, Map.Entry<String,
            File> entry) {

        String j2filename = entry.getKey() + MigrationConstants.J2_FILE_EXTENSION;
        return defaultXMLFiles.get(entry.getKey()) != null && j2files.get(j2filename) != null;
    }

    /**
     * Compare xml files using xmlUnit2 library.
     *
     * @param defaultFile Default xml file.
     * @param migratedFile Migrated xml file.
     * @return Diff of difference.
     */
    private Diff compareXMLFiles(File defaultFile, File migratedFile)  {

        try {

            return DiffBuilder.compare(migratedFile).withTest(defaultFile)
                    .ignoreWhitespace()
                    .ignoreComments()
                    .checkForSimilar()
                    .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
                    .build();
        } catch (XMLUnitException e) {
            log.error("Error occurred when comparing default File : " + defaultFile.getName() + " and migrated" +
                    " file :" + migratedFile.getName());
        }
        return null;
    }

    /**
     * Compare difference to print the output.
     *
     * @param migratedFile    Migrated xml file.
     * @param detailedDiff    Diff of all the difference.
     * @param existingXMLTags Map of existing xml tags.
     * @param keyValues       Map of key values.
     * @param outputGenerator OutputGenerator.
     */
    private void getDifferenceToMaps(File migratedFile,
                                     Diff detailedDiff,
                                     Map<String, String> existingXMLTags,
                                     Map<String, String> keyValues,
                                     OutputGenerator outputGenerator) {

        if (detailedDiff != null) {
            Iterable<Difference> differences = detailedDiff.getDifferences();
            for (Difference difference : differences) {
                if (difference.getResult().name().equals("DIFFERENT")) {
                    String csvEntry = null;
                    String csvKey;
                    String defaultValue = " ";
                    String changedValue = " ";

                    csvKey = difference.getComparison().getControlDetails().getXPath();
                    if (!isMigratedXPathInDiffContainsText(difference)) {
                        if (isMigratedFileNotContainPropertiesWhichDefaultHas(difference)) {
                            continue;
                        } else {
                            csvEntry =
                                    migratedFile.getName().concat(MigrationConstants.CSV_SEPARATOR_APPENDER)
                                            .concat(MigrationConstants.XML_FILE_TYPE)
                                            .concat(MigrationConstants.CSV_SEPARATOR_APPENDER).concat(csvKey)
                                            .concat("| | | |").concat(defaultValue).concat("| | Not In Default");
                        }
                    }

                    if (StringUtils.isNotBlank(csvKey)) {
                        if (difference.getComparison().getControlDetails().getTarget() != null) {
                            changedValue = difference.getComparison().getControlDetails().getTarget().getNodeValue();
                        }
                        if (isContainDefaultValue(difference)) {
                            defaultValue = difference.getComparison().getTestDetails().getTarget().getNodeValue();
                        }
                        if (isXpathNotInExistingTags(existingXMLTags, csvKey)) {
                            if (StringUtils.isBlank(csvEntry)) {
                                csvEntry =
                                        migratedFile.getName().concat(MigrationConstants.CSV_SEPARATOR_APPENDER)
                                                .concat(MigrationConstants.XML_FILE_TYPE)
                                                .concat(MigrationConstants.CSV_SEPARATOR_APPENDER).concat(csvKey)
                                                .concat("| | | |").concat(defaultValue).concat("| |");
                            }
                            existingXMLTags.put(csvKey, csvEntry);
                            outputGenerator.setGenerateToml(false);
                        }
                        keyValues.put(csvKey, changedValue);
                    }
                }
            }
        }
    }

    private boolean isContainDefaultValue(Difference difference) {

        return difference.getComparison().getTestDetails().getTarget() != null
                && difference.getComparison().getTestDetails().getTarget().getNodeValue() != null;
    }

    private boolean isMigratedFileNotContainPropertiesWhichDefaultHas(Difference difference) {

        return difference.getComparison().getControlDetails().getXPath() == null
                && difference.getComparison().getTestDetails().getXPath() != null;
    }

    private boolean isXpathNotInExistingTags(Map<String, String> existingXMLTags, String tag) {

        return existingXMLTags.get(tag) == null;
    }

    private boolean isMigratedXPathInDiffContainsText(Difference diff) {

        // Possible xpath values that will be templated.
        String[] xpathValues = {"@class", "text()"};
        return diff.getComparison().getControlDetails().getXPath() != null && Arrays.stream(xpathValues).anyMatch(
                diff.getComparison().getControlDetails().getXPath()::contains);
    }

    private void filterUnTemplatedFiles(Map<String, File> defaultXMLFiles, Map<String, File> migratedXMLFiles,
                                        File logFile, Map.Entry<String, File> entry) throws IOException {

        if (migratedXMLFiles.get(entry.getKey()) != null && defaultXMLFiles.get(entry.getKey()) != null) {
            if (FileUtils.contentEquals(migratedXMLFiles.get(entry.getKey()),
                    defaultXMLFiles.get(entry.getKey()))) {
                Files.write(Paths.get(logFile.getPath()), (entry.getValue().getPath() + " is not templated " +
                        "with toml. \n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                File outFile =
                        new File(MigrationConstants.UN_TEMPLATE_FILE_FOLDER + MigrationConstants
                                .FILE_SEPARATOR + entry.getKey());
                FileUtils.copyFile(entry.getValue(), outFile);
            }
        }
    }
}

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

package org.wso2.config.diff.creater;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;

import org.wso2.config.diff.creater.utils.MigrationConstants;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Main Class for migration configs.
 */
class ConfigDiffChecker {

    private static final Log log = LogFactory.getLog(ConfigDiffChecker.class);

    /**
     * Find diff of XML and Properties files and write to output CSV files.
     *
     * @param configLoader    ConfigLoader which has separated file maps.
     * @param outputGenerator OutputGenerator class.
     * @throws IOException  IOException.
     * @throws SAXException SAXException.
     */
    public void findConfigDiff(ConfigLoader configLoader, OutputGenerator outputGenerator) throws IOException,
            SAXException {

        Map<String, String> existingTags = configLoader.readFromCSV(new URL(MigrationConstants.CATALOG_URL));

        checkXMLDiff(configLoader.getDefaultXMLFiles(), configLoader.getMigratedXMLFiles(),
                configLoader.getJ2TemplateFiles(), existingTags, outputGenerator.getOutputCSV(),
                outputGenerator.getOutputKeyValues());

        checkPropertyDiff(configLoader.getDefaultPropertiesFiles(), configLoader.getMigratedPropertiesFiles(),
                configLoader.getJ2TemplateFiles(), existingTags, outputGenerator.getOutputCSV(),
                outputGenerator.getOutputKeyValues());
    }

    /**
     * Check whether property files are in templates or not, if true find diff.
     *
     * @param defaultPropertiesFiles  default property files map.
     * @param migratedPropertiesFiles migrated property files map.
     * @param j2TemplateFiles         template property files .
     * @param existingTags            map of existing knowledge.
     * @param outputCSVFile           output csv file.
     * @param keyValueFile            output key-value file.
     * @throws IOException IOException
     */
    private void checkPropertyDiff(Map<String, File> defaultPropertiesFiles,
                                   Map<String, File> migratedPropertiesFiles, Map<String, File> j2TemplateFiles,
                                   Map<String, String> existingTags, File outputCSVFile, File keyValueFile)
            throws IOException {

        for (Map.Entry<String, File> entry : migratedPropertiesFiles.entrySet()) {

            if (isFileTemplated(defaultPropertiesFiles, j2TemplateFiles, entry)) {

                Set<Map.Entry<String, String>> changedPropertyDiffSet =
                        findDiffPropertiesFiles(defaultPropertiesFiles.get(entry.getKey()),
                                migratedPropertiesFiles.get(entry.getKey()));
                writePropertiesDiffToCSV(migratedPropertiesFiles.get(entry.getKey()), existingTags,
                        Paths.get(outputCSVFile.getPath()), Paths.get(keyValueFile.getPath()), changedPropertyDiffSet);
            } else {

                log.warn(entry.getValue().getPath() + " is not templated with toml. \n");
                File outFile =
                        new File(MigrationConstants.UN_TEMPLATE_FILE_FOLDER + MigrationConstants
                                .FILE_SEPARATOR + entry.getKey());
                FileUtils.copyFile(entry.getValue(), outFile);
            }
        }
    }


    /**
     * Find the difference of property files.
     *
     * @param defaultFile  default file to compare with.
     * @param migratedFile migrated file used to compare.
     * @return A set of difference.
     * @throws IOException IOException.
     */
    private Set<Map.Entry<String, String>> findDiffPropertiesFiles(File defaultFile, File migratedFile)
            throws IOException {

        Properties defaultProperties = new Properties();
        Properties changedProperties = new Properties();
        try (FileInputStream defaultInputStream = new FileInputStream(defaultFile);
             FileInputStream changedInputStream = new FileInputStream(migratedFile)) {

            defaultProperties.load(defaultInputStream);
            changedProperties.load(changedInputStream);
            Map<String, String> defaultMap = new HashMap<String, String>((Map) defaultProperties);
            Map<String, String> changedMap = new HashMap<String, String>((Map) changedProperties);

            Set<Map.Entry<String, String>> defaultPropertySet = defaultMap.entrySet();
            Set<Map.Entry<String, String>> changedPropertySet = changedMap.entrySet();
            // Leaves only entries in changedPropertySet that are only in changedPropertySet, not in defaultPropertySet.
            changedPropertySet.removeAll(defaultPropertySet);

            return changedPropertySet;
        } catch (IOException e) {
            throw new IOException("The configurations can not be loaded properly. Please try again. ");
        }
    }

    /**
     * Write difference to csv files.
     *
     * @param migratedFile       migrated file.
     * @param existingTags       existing tags in the knowledge base.
     * @param outputCSVFilePath  output csv file path.
     * @param keyValueFilePath   key-value csv file path.
     * @param changedPropertySet property difference.
     * @throws IOException IOException.
     */
    private void writePropertiesDiffToCSV(File migratedFile, Map<String, String> existingTags, Path outputCSVFilePath,
                                          Path keyValueFilePath, Set<Map.Entry<String, String>> changedPropertySet)
            throws IOException {

        for (Map.Entry<String, String> property : changedPropertySet) {

            String keyValueData;
            String csvEntry;

            if (existingTags.get(property.getKey()) == null) {

                csvEntry = migratedFile.getName().concat(MigrationConstants.CSV_SEPARATOR_APPENDER).concat(
                        "properties").concat(MigrationConstants.CSV_SEPARATOR_APPENDER).concat(property.getKey())
                        .concat("| | | | | |").concat(MigrationConstants.NEW_LINE);
                existingTags.put(property.getKey(), csvEntry);

                log.info("Add this entry to the remote catalog : " + csvEntry + MigrationConstants.NEW_LINE);

                keyValueData =
                        property.getKey().concat(MigrationConstants.CSV_SEPARATOR_APPENDER).concat(property.getValue())
                                .concat(MigrationConstants.NEW_LINE);
            } else {
                csvEntry = existingTags.get(property.getKey()).concat(MigrationConstants.NEW_LINE);

                keyValueData =
                        property.getKey().concat(MigrationConstants.CSV_SEPARATOR_APPENDER).concat(property.getValue())
                                .concat(MigrationConstants.NEW_LINE);
            }

            Files.write(outputCSVFilePath, csvEntry.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            Files.write(keyValueFilePath, keyValueData.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }
    }


    private void checkXMLDiff(Map<String, File> defaultXMLFiles, Map<String, File> migratedXMLFiles, Map<String,
            File> j2files, Map<String, String> existingTags, File csvOutput, File keyValueOutput) throws IOException,
            SAXException {

        for (Map.Entry<String, File> entry : migratedXMLFiles.entrySet()) {

            if (isFileTemplated(defaultXMLFiles, j2files, entry)) {

                DetailedDiff detailedDiff = compareXMLFiles(defaultXMLFiles.get(entry.getKey()),
                        migratedXMLFiles.get(entry.getKey()));
                writeDifferenceToCSV(migratedXMLFiles.get(entry.getKey()), existingTags, csvOutput, keyValueOutput,
                        detailedDiff);
            } else {

                // Copying the changed un-template files to a different folder.
                log.warn(entry.getValue().getPath() + " is not templated with toml. \n");
                File outFile =
                        new File(MigrationConstants.UN_TEMPLATE_FILE_FOLDER + MigrationConstants
                                .FILE_SEPARATOR + entry.getKey());
                FileUtils.copyFile(entry.getValue(), outFile);
            }
        }
    }

    private boolean isFileTemplated(Map<String, File> defaultXMLFiles, Map<String, File> j2files, Map.Entry<String,
            File> entry) {

        String j2filename = entry.getKey() + ".j2";
        return defaultXMLFiles.get(entry.getKey()) != null && j2files.get(j2filename) != null;
    }

    /**
     * Compare xml files using xmlUnit library.
     *
     * @param defaultFile  default xml file to compare with.
     * @param migratedFile migrated xml file to compare.
     * @return DetailedDiff of xml difference.
     * @throws IOException  IOException.
     * @throws SAXException SAXException.
     */
    private DetailedDiff compareXMLFiles(File defaultFile, File migratedFile) throws IOException, SAXException {

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);

        InputSource defaultSource = new InputSource(defaultFile.getPath());
        InputSource changedSource = new InputSource(migratedFile.getPath());

        DetailedDiff detailedDiff = new DetailedDiff(new Diff(defaultSource, changedSource));
        detailedDiff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());

        return detailedDiff;
    }

    /**
     * Write the diff to the output CSV files.
     *
     * @param migratedFile    migrated xml file.
     * @param existingXMLTags existing tags from the knowledge.
     * @param csvOutput       main output csv catalog.
     * @param keyValueOutput  key-value csv file.
     * @param detailedDiff    objects of Differences.
     * @throws IOException FileNotFound Exception.
     */
    private void writeDifferenceToCSV(File migratedFile, Map<String, String> existingXMLTags, File csvOutput,
                                      File keyValueOutput, DetailedDiff detailedDiff) throws IOException {

        final String fileType = "xml";
        Path outputPath = Paths.get(csvOutput.getPath());
        Path keyValPath = Paths.get(keyValueOutput.getPath());

        for (Object diffObject : detailedDiff.getAllDifferences()) {

            Difference diff = (Difference) diffObject;
            String csvEntry;
            String keyVal;
            String csvKey = "";
            String defaultValue = "";
            String changedValue;

            if (!isMigratedXPathAvailableInDiff(diff)) {
                continue;
            }

            if (isXpathInExistingTags(existingXMLTags, diff)) {
                if (isMigratedXpathEqualsDefaultXpath(diff)) {

                    csvKey = diff.getControlNodeDetail().getXpathLocation();
                    defaultValue = diff.getControlNodeDetail().getValue();
                } else {

                    if (StringUtils.isNotBlank(diff.getTestNodeDetail().getXpathLocation())) {

                        csvKey = diff.getTestNodeDetail().getXpathLocation();
                        defaultValue = " ";
                    }
                }
                changedValue = diff.getTestNodeDetail().getValue();

                csvEntry =
                        migratedFile.getName().concat(MigrationConstants.CSV_SEPARATOR_APPENDER).concat(fileType)
                                .concat(MigrationConstants.CSV_SEPARATOR_APPENDER).concat(csvKey).concat("| | | |")
                                .concat(defaultValue).concat("| |").concat(MigrationConstants.NEW_LINE);

                existingXMLTags.put(csvKey, csvEntry);

                keyVal =
                        csvKey.concat(MigrationConstants.CSV_SEPARATOR_APPENDER).concat(changedValue)
                                .concat(MigrationConstants.NEW_LINE);

                log.info("Add this entry to the remote catalog : " + csvEntry + MigrationConstants.NEW_LINE);
            } else {
                csvEntry =
                        existingXMLTags.get(diff.getTestNodeDetail().getXpathLocation())
                                .concat(MigrationConstants.NEW_LINE);
                // Writing to keyValue csv.
                keyVal =
                        diff.getTestNodeDetail().getXpathLocation().concat(MigrationConstants
                                .CSV_SEPARATOR_APPENDER).concat(diff.getTestNodeDetail().getValue())
                                .concat(MigrationConstants.NEW_LINE);
            }
            Files.write(outputPath, csvEntry.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            Files.write(keyValPath, keyVal.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }

    }

    private boolean isMigratedXpathEqualsDefaultXpath(Difference diff) {

        return diff.getTestNodeDetail().getXpathLocation().equals(diff.getControlNodeDetail().getXpathLocation());
    }

    private boolean isXpathInExistingTags(Map<String, String> existingXMLTags, Difference diff) {

        return existingXMLTags.get(diff.getTestNodeDetail().getXpathLocation()) == null;
    }

    private boolean isMigratedXPathAvailableInDiff(Difference diff) {

        String[] xpathValues = {"@class", "text()"};
        return diff.getTestNodeDetail().getXpathLocation() != null && Arrays.stream(xpathValues).anyMatch(diff
                .getTestNodeDetail().getXpathLocation()::contains);
    }
}

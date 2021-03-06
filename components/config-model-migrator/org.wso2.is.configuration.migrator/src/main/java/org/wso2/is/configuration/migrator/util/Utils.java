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

package org.wso2.is.configuration.migrator.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.is.configuration.diff.creator.exception.ConfigMigrationException;
import org.wso2.is.configuration.diff.creator.utils.MigrationConstants;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

/**
 * The utility methods used in migration tool.
 */
public class Utils {

    private static final Logger log = LogManager.getLogger(Utils.class);

    public static File getTomlFile(String homePath) {

        return new File(homePath + MigrationConstants.CONF_PATH + MigrationConstants.FILE_SEPARATOR
                + MigrationConstants.DEPLOYMENT_TOML_FILE);
    }

    /**
     * Write the information to the catalog file.
     *
     * @param keyCatalogValuesMap Map of catalog keys and values.
     * @param outputCSV           The output csv file.
     * @throws ConfigMigrationException ConfigMigrationException.
     */
    public static void writeToFile(Map<String, String> keyCatalogValuesMap, File outputCSV)
            throws ConfigMigrationException {

        if (keyCatalogValuesMap != null) {
            try {
                Files.write(Paths.get(outputCSV.getPath()), MigrationConstants.CATALOG_FIRST_ENTRY.concat(
                        MigrationConstants.NEW_LINE).getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
                for (Map.Entry<String, String> catalogEntry : keyCatalogValuesMap.entrySet()) {
                    Files.write(Paths.get(outputCSV.getPath()), catalogEntry.getValue().concat(MigrationConstants
                            .NEW_LINE).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                }
            } catch (IOException e) {
                throw new ConfigMigrationException("Error occurred when writing catalog file.", e);
            }
        }
    }
}

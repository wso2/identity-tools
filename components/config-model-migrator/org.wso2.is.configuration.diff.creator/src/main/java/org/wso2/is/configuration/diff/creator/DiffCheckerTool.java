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

package org.wso2.is.configuration.diff.creator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.is.configuration.diff.creator.exception.ConfigMigrationException;

/**
 * The main class for diff finding tool.
 */
public class DiffCheckerTool {

    private static final Logger log = LogManager.getLogger(DiffCheckerTool.class);

    /**
     * Create diff from the configurations.
     *
     * @param migratedISHomePath migrated IS Home Path.
     * @param defaultISHomePath  default IS Home Path.
     * @throws ConfigMigrationException ConfigMigrationException.
     */
    public OutputGenerator createDiff(String migratedISHomePath, String defaultISHomePath)
            throws ConfigMigrationException {

        ConfigLoader configLoader = new ConfigLoader();
        configLoader.validate(migratedISHomePath, defaultISHomePath);
        log.info("The migrated IS-Home path and default IS-Home path are successfully validated.");
        configLoader.filterFiles(migratedISHomePath, defaultISHomePath);

        OutputGenerator outputGenerator = new OutputGenerator();
        outputGenerator.createOutputFiles();

        ConfigurationDiffChecker diffChecker = new ConfigurationDiffChecker();
        diffChecker.findConfigDiff(configLoader, outputGenerator);
        return outputGenerator;
    }
}

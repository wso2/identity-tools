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

package org.wso2.configuration.diff.creater;

import org.wso2.configuration.diff.creater.exception.ConfigMigrateException;
import org.xml.sax.SAXException;
import java.io.IOException;

/**
 * The main class for diff finding tool.
 */
public class DiffCheckerTool {

    /**
     * Main method to run the tool.
     *
     * @param args args[0] = migrated Identity Server home path, args[1]= default Identity Server home path.
     * @throws ConfigMigrateException ConfigMigrateException
     * @throws IOException IOException
     * @throws SAXException SAXException
     */
    public static void main(String[] args) throws ConfigMigrateException, IOException, SAXException {

        String migratedISHomePath = args[0];
        String defaultISHomePath = args[1];

        ConfigLoader configLoader = new ConfigLoader();
        configLoader.validate(migratedISHomePath, defaultISHomePath);
        configLoader.filterFiles(migratedISHomePath, defaultISHomePath);

        OutputGenerator outputGenerator = new OutputGenerator();
        outputGenerator.createOutputFiles();

        ConfigurationDiffChecker diffChecker = new ConfigurationDiffChecker();
        diffChecker.findConfigDiff(configLoader, outputGenerator);
    }
}

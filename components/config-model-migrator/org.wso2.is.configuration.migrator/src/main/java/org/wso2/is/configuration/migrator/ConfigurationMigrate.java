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

package org.wso2.is.configuration.migrator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.is.configuration.diff.creater.DiffCheckerTool;
import org.wso2.is.configuration.diff.creater.OutputGenerator;
import org.wso2.is.configuration.diff.creater.exception.ConfigMigrateException;
import org.wso2.is.configuration.migrator.util.Utils;
import org.wso2.is.configuration.toml.generator.TomlGeneratorTool;

/**
 * Main class for the migration tool.
 */
public class ConfigurationMigrate {

    private static final Logger log = LogManager.getLogger(ConfigurationMigrate.class);

    /**
     * The main method which runs when running the shell-script.
     *
     * @param args args[0] = tool type, args[1] = migrated IS home path, args[2] = default IS home path.
     * @throws ConfigMigrateException ConfigMigrateException.
     */
    public static void main(String args[]) throws ConfigMigrateException {

        String migrateISHomePath;
        String defaultISHomePath;
        if (args.length == 2) {
            migrateISHomePath = args[0];
            defaultISHomePath = args[1];
        } else {
            log.error("Please provide migrated IS-Home path and the default IS-Home path.");
            throw new ConfigMigrateException("Please provide migrated IS-Home path and the default IS-Home path.");
        }
        DiffCheckerTool diffCheckTool = new DiffCheckerTool();
        TomlGeneratorTool tomlGenTool = new TomlGeneratorTool();

        OutputGenerator outGen;
        outGen = diffCheckTool.createDiff(migrateISHomePath, defaultISHomePath);
        if (outGen.isGenerateToml()) {
            tomlGenTool.generateTomlFile(outGen.getKeyValuesMap(), Utils.getTomlFile(defaultISHomePath),
                    outGen.getLogFile());
            log.info("New config migration is successfully completed!!!");
            log.info("=================== END of the Tool =============================================");
        } else {
            Utils.writeToFile(outGen.getKeyCatalogValuesMap(), outGen.getOutputCSV());
            log.error("We have figured out some configs which does not have toml configurations. \n ");
            log.info("Please update the 'output/OutputCatalog.csv' file with toml configurations and commit" +
                    " it to the Github and re-run the tool. \n");
            log.info("=================== Complete and Try again to generate toml ================================");
        }
    }
}

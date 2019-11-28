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
import org.wso2.is.configuration.diff.creater.utils.MigrationConstants;
import org.wso2.is.configuration.migrator.util.Utils;
import org.wso2.is.configuration.toml.generator.TomlGeneratorTool;
import org.wso2.is.configuration.toml.generator.util.TomlGeneratorConstants;

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
        Utils.writeToFile(outGen.getKeyCatalogValuesMap(), outGen.getOutputCSV());
        tomlGenTool.generateTomlFile(outGen.getKeyValuesMap(), Utils.getTomlFile(defaultISHomePath),
                outGen.getLogFile());
        if (outGen.isGenerateToml()) {
            log.info("=================================================================================");
            log.info("|         New config migration is successfully completed!!!                     |");
            log.info("=================================================================================");
            log.info("\n\n Untemplated files with changes available in " + MigrationConstants
                    .UN_TEMPLATE_FILE_FOLDER);
            log.info("\n Logs on further output is available in " + MigrationConstants.LOG_FILE);
            log.info("\n Updated deployment.toml file is available in " + TomlGeneratorConstants
                    .UPDATED_DEPLOYMENT_TOML);
            log.info("\n\n----------------------------------------Good Bye!!--------------------------------");
        } else {
            log.info("=================================================================================");
            log.info("|        New config migration Failed !!!    New Configs Found!!                 |");
            log.info("=================================================================================");

            log.info("\n \n A deployment.toml file has generated with the existing configs.");
            log.info("\n \n ** If you need to generate the complete toml, add the toml configs to the " +
                    MigrationConstants.OUTPUT_CATALOG_CSV + " file, commit it to the github and re-run the tool");
            log.info("OR  Add these changes found in " + MigrationConstants.OUTPUT_CATALOG_CSV +
                    ", to the deployment.toml manually.");
            log.info("\n\n----------------------------------------Good Bye!!--------------------------------");
        }
    }
}

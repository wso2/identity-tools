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

package org.wso2.is.configuration.toml.generator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.is.configuration.diff.creater.exception.ConfigMigrateException;
import org.wso2.is.configuration.toml.generator.util.TomlGenerator;
import java.io.File;
import java.util.Map;


/**
 * Main class for the toml file generation tool.
 */
public class TomlGeneratorTool {

    private static final Log log = LogFactory.getLog(TomlGeneratorTool.class);

    public void generateTomlFile(Map<String, String> keyValuesMapFromDiff, File deploymentTomlFile, File log)
            throws ConfigMigrateException {

        TomlGenerator tomlGenerator = new TomlGenerator();
        Map<String, Object> tomlKeyValueMap = tomlGenerator.generateTomlKeyValueMap(keyValuesMapFromDiff,
                deploymentTomlFile, log);
        TomlGenerator.writeToTOMLFile(tomlKeyValueMap);

    }
}

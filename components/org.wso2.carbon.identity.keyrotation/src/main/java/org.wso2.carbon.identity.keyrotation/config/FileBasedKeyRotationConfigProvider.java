/*
 * Copyright (c) (2021-2024), WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.keyrotation.config;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.wso2.carbon.identity.keyrotation.config.model.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.dao.DBConstants;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A file-based provider implementation for the {@link KeyRotationConfig} model.
 * It is an implementation of the base class, {@link KeyRotationConfigProvider}.
 */
public class FileBasedKeyRotationConfigProvider implements KeyRotationConfigProvider {

    private static final Logger logger = Logger.getLogger(FileBasedKeyRotationConfigProvider.class);
    private KeyRotationConfig keyRotationConfig = new KeyRotationConfig();

    public FileBasedKeyRotationConfigProvider(InputStream fileInputStream) throws KeyRotationException {

        Properties properties = new Properties();
        try {
            properties.load(fileInputStream);
        } catch (IOException e) {
            throw new KeyRotationException("Error occurred while loading the yaml file: ", e);
        }
        initConfig(properties);
    }

    private void initConfig(Properties properties) throws KeyRotationException {

        KeyRotationConfigValidator configValidator = KeyRotationConfigValidator.getInstance();

        String oldSecretKey = properties.getProperty(KeyRotationConstants.OLD_SECRET_KEY);
        String newSecretKey = properties.getProperty(KeyRotationConstants.NEW_SECRET_KEY);
        String ishome = properties.getProperty(KeyRotationConstants.IS_HOME);
        String idnDBUrl = properties.getProperty(KeyRotationConstants.IDN_DB_URL);
        String idnUsername = properties.getProperty(KeyRotationConstants.IDN_USERNAME);
        String idnPassword = properties.getProperty(KeyRotationConstants.IDN_PASSWORD);
        String regDBUrl = properties.getProperty(KeyRotationConstants.REG_DB_URL);
        String regUsername = properties.getProperty(KeyRotationConstants.REG_USERNAME);
        String regPassword = properties.getProperty(KeyRotationConstants.REG_PASSWORD);
        String enableDBMigrator = properties.getProperty(KeyRotationConstants.ENABLE_DB_MIGRATOR);
        String enableConfigMigrator = properties.getProperty(KeyRotationConstants.ENABLE_CONFIG_MIGRATOR);
        String enableWorkFlowMigrator = properties.getProperty(KeyRotationConstants.ENABLE_WORKFLOW_MIGRATOR);
        try {
            int chunkSize = StringUtils.isNotBlank(properties.getProperty(KeyRotationConstants.CHUNK_SIZE)) ?
                    Integer.parseInt(properties.getProperty(KeyRotationConstants.CHUNK_SIZE)) :
                    DBConstants.DEFAULT_CHUNK_SIZE;
            chunkSize = (chunkSize < 1) ? DBConstants.DEFAULT_CHUNK_SIZE : chunkSize;
            keyRotationConfig.setChunkSize(chunkSize);
        } catch (NumberFormatException e) {
            logger.log(Level.WARN, "Not a valid number. Falling back to default chunk size.", e);
            keyRotationConfig.setChunkSize(DBConstants.DEFAULT_CHUNK_SIZE);
        }

        configValidator.validateFilePath(KeyRotationConstants.IS_HOME, ishome);
        configValidator.validateURI(KeyRotationConstants.IDN_DB_URL, idnDBUrl);
        configValidator.validateURI(KeyRotationConstants.REG_DB_URL, regDBUrl);
        configValidator.validateBoolean(KeyRotationConstants.ENABLE_DB_MIGRATOR, enableDBMigrator);
        configValidator.validateBoolean(KeyRotationConstants.ENABLE_CONFIG_MIGRATOR, enableConfigMigrator);
        configValidator.validateBoolean(KeyRotationConstants.ENABLE_WORKFLOW_MIGRATOR, enableWorkFlowMigrator);

        keyRotationConfig.setOldSecretKey(oldSecretKey);
        keyRotationConfig.setNewSecretKey(newSecretKey);
        keyRotationConfig.setISHome(ishome);
        keyRotationConfig.setIdnDBUrl(idnDBUrl);
        keyRotationConfig.setIdnUsername(idnUsername);
        keyRotationConfig.setIdnPassword(idnPassword);
        keyRotationConfig.setRegDBUrl(regDBUrl);
        keyRotationConfig.setRegUsername(regUsername);
        keyRotationConfig.setRegPassword(regPassword);
        keyRotationConfig.setEnableDBMigrator(Boolean.parseBoolean(enableDBMigrator));
        keyRotationConfig.setEnableConfigMigrator(Boolean.parseBoolean(enableConfigMigrator));
        keyRotationConfig.setEnableWorkflowMigrator(Boolean.parseBoolean(enableWorkFlowMigrator));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeyRotationConfig getKeyRotationConfig() {

        return keyRotationConfig;
    }
}

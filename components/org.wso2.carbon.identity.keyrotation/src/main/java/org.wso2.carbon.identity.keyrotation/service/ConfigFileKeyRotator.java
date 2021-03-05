/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.keyrotation.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.keyrotation.config.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.io.File;
import java.util.List;

import static org.wso2.carbon.identity.keyrotation.util.ConfigFileUtil.getFilePaths;
import static org.wso2.carbon.identity.keyrotation.util.ConfigFileUtil.getFolderPaths;
import static org.wso2.carbon.identity.keyrotation.util.ConfigFileUtil.updateConfigFile;
import static org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants.DEPLOYMENT;
import static org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants.EVENT_PUBLISHERS;
import static org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants.REPOSITORY;
import static org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants.SERVER;
import static org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants.TEANANTS;
import static org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants.USERSTORES;

/**
 * Config file reEncryption service.
 */
public class ConfigFileKeyRotator {

    private static final Log log = LogFactory.getLog(ConfigFileKeyRotator.class);
    private static final ConfigFileKeyRotator instance = new ConfigFileKeyRotator();
    private static final String userstoreProperty = "Property";
    private static final String publisherProperty = "property";

    public static ConfigFileKeyRotator getInstance() {

        return instance;
    }

    /**
     * ReEncryption of the configuration file data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public void configFileReEncryptor(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        checkNewISHomePathValid(keyRotationConfig);
        log.info("Re-encrypting configuration file data...");
        reEncryptSuperTenantUserStore(keyRotationConfig);
        reEncryptTenantUserStore(keyRotationConfig);
        reEncryptEventPublishers(keyRotationConfig);
        log.info("Re-encrypting configuration file data completed...\n");
    }

    /**
     * To check whether the loaded new IS home path exists.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    private void checkNewISHomePathValid(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        File file = new File(keyRotationConfig.getNewISHome());
        if (!file.exists()) {
            throw new KeyRotationException("Error occurred while finding new IS home path");
        }
    }

    /**
     * ReEncryption of the super tenant user store passwords in configuration files.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    private void reEncryptSuperTenantUserStore(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        File[] superTenantUserStoreFiles =
                getFilePaths(keyRotationConfig.getNewISHome(), REPOSITORY, DEPLOYMENT, SERVER, USERSTORES);
        for (File file : superTenantUserStoreFiles) {
            updateConfigFile(file, keyRotationConfig, userstoreProperty);
        }
    }

    /**
     * ReEncryption of the tenant user store passwords in configuration files.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    private void reEncryptTenantUserStore(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        List<String> tenants =
                getFolderPaths(keyRotationConfig.getNewISHome(), REPOSITORY, TEANANTS);
        for (String folder : tenants) {
            File[] tenantUserStoreFiles = getFilePaths(folder + USERSTORES);
            for (File file : tenantUserStoreFiles) {
                updateConfigFile(file, keyRotationConfig, userstoreProperty);
            }
        }
    }

    /**
     * ReEncryption of the event publisher passwords in configuration files.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    private void reEncryptEventPublishers(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        File[] eventPublisherFiles =
                getFilePaths(keyRotationConfig.getNewISHome() + REPOSITORY + DEPLOYMENT + SERVER + EVENT_PUBLISHERS);
        for (File file : eventPublisherFiles) {
            updateConfigFile(file, keyRotationConfig, publisherProperty);
        }
    }
}

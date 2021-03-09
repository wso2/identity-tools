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
package org.wso2.carbon.identity.keyrotation.config;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationServiceUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Paths;

/**
 * Class to load the configurations in the properties.yaml file.
 */
public class KeyRotationConfig {

    private String oldSecretKey;
    private String newSecretKey;
    private String oldISHome;
    private String newISHome;
    private String idnDBUrl;
    private String idnUsername;
    private String idnPassword;
    private String regDBUrl;
    private String regUsername;
    private String regPassword;
    private String enableDBMigrator;
    private String enableConfigMigrator;
    private String enableSyncMigrator;
    private static final Log log = LogFactory.getLog(KeyRotationConfig.class);
    private static final KeyRotationConfig instance = new KeyRotationConfig();

    public static KeyRotationConfig getInstance() {

        return instance;
    }

    /**
     * Load the configurations placed in the properties.yaml file.
     *
     * @return KeyRotation config object.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public KeyRotationConfig loadConfigs(String[] args) throws KeyRotationException {

        String propertiesFilePath = Paths.get("components", new String[]{
                "org.wso2.carbon.identity.keyrotation", "src", "main", "resources", "properties.yaml"}).toString();
        if (args.length > 0) {
            propertiesFilePath = args[0];
        }
        File file = new File(propertiesFilePath);
        if (!file.exists()) {
            throw new KeyRotationException(
                    "Error occurred, properties.yaml file not found in provided path, " + propertiesFilePath);
        }
        log.info("Loading Key Rotation Configs from absolute path: " + propertiesFilePath);
        KeyRotationConfig keyRotationConfig =
                KeyRotationServiceUtils.loadKeyRotationConfig(propertiesFilePath);
        checkKeyRotationConfigs(keyRotationConfig);
        log.info("Successfully loaded the config file.");
        return keyRotationConfig;
    }

    /**
     * To check whether the loaded configurations are valid and not null.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public void checkKeyRotationConfigs(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        Field[] props = KeyRotationConfig.getInstance().getClass().getDeclaredFields();
        try {
            for (int i = 0; i < props.length - 2; i++) {
                if (props[i].get(keyRotationConfig) == null) {
                    throw new KeyRotationException(
                            "Error occurred, null value found in property, " + props[i].getName());
                }
                if (props[i].getName().equals("oldISHome") || props[i].getName().equals("newISHome")) {
                    File file = new File(props[i].get(keyRotationConfig).toString());
                    if (!file.exists()) {
                        throw new KeyRotationException(
                                "Error occurred while finding " + props[i].getName() + " path.");
                    }
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new KeyRotationException("Error occurred while checking for null values in the loaded properties " +
                    "file, ", e);
        }
    }

    public String getOldSecretKey() {

        return oldSecretKey;
    }

    public void setOldSecretKey(String oldSecretKey) {

        this.oldSecretKey = oldSecretKey;
    }

    public String getNewSecretKey() {

        return newSecretKey;
    }

    public void setNewSecretKey(String newSecretKey) {

        this.newSecretKey = newSecretKey;
    }

    public String getOldISHome() {

        return oldISHome;
    }

    public void setOldISHome(String oldISHome) {

        this.oldISHome = oldISHome;
    }

    public String getNewISHome() {

        return newISHome;
    }

    public void setNewISHome(String newISHome) {

        this.newISHome = newISHome;
    }

    public String getIdnDBUrl() {

        return idnDBUrl;
    }

    public void setIdnDBUrl(String idnDBUrl) {

        this.idnDBUrl = idnDBUrl;
    }

    public String getIdnUsername() {

        return idnUsername;
    }

    public void setIdnUsername(String idnUsername) {

        this.idnUsername = idnUsername;
    }

    public String getIdnPassword() {

        return new String(Base64.decode(idnPassword));
    }

    public void setIdnPassword(String password) {

        this.idnPassword = password;
    }

    public String getRegDBUrl() {

        return regDBUrl;
    }

    public void setRegDBUrl(String regDBUrl) {

        this.regDBUrl = regDBUrl;
    }

    public String getRegUsername() {

        return regUsername;
    }

    public void setRegUsername(String regUsername) {

        this.regUsername = regUsername;
    }

    public String getRegPassword() {

        return new String(Base64.decode(regPassword));
    }

    public void setRegPassword(String regPassword) {

        this.regPassword = regPassword;
    }

    public String getEnableDBMigrator() {

        return enableDBMigrator;
    }

    public void setEnableDBMigrator(String enableDBMigrator) {

        this.enableDBMigrator = enableDBMigrator;
    }

    public String getEnableConfigMigrator() {

        return enableConfigMigrator;
    }

    public void setEnableConfigMigrator(String enableConfigMigrator) {

        this.enableConfigMigrator = enableConfigMigrator;
    }

    public String getEnableSyncMigrator() {

        return enableSyncMigrator;
    }

    public void setEnableSyncMigrator(String enableSyncMigrator) {

        this.enableSyncMigrator = enableSyncMigrator;
    }

    @Override
    public String toString() {

        return "\noldSecretKey: " + oldSecretKey + "\nnewSecretKey: " + newSecretKey + "\noldISHome: " + oldISHome +
                "\nnewISHome: " + newISHome + "\nidnDBUrl: " + idnDBUrl + "\nidnUsername: " + idnUsername +
                "\nidnPassword: " + idnPassword + "\nregDBUrl: " + regDBUrl + "\nregUsername: " + regUsername +
                "\nregPassword: " + regPassword + "\nenableDBMigrator: " + enableDBMigrator +
                "\nenableConfigMigrator: " + enableConfigMigrator + "\nenableSyncMigrator: " + enableSyncMigrator +
                "\n";
    }
}

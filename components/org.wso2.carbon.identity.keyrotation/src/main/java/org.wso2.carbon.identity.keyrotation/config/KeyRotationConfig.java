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

        String propertiesFilePath = Paths.get("components", "org.wso2.carbon.identity.keyrotation", "src", "main",
                "resources", "properties.yaml").toString();
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

    /**
     * Getter to get the old secret key.
     *
     * @return Old secret key.
     */
    public String getOldSecretKey() {

        return oldSecretKey;
    }

    /**
     * Setter to set the old secret key.
     *
     * @param oldSecretKey Old secret key.
     */
    public void setOldSecretKey(String oldSecretKey) {

        this.oldSecretKey = oldSecretKey;
    }

    /**
     * Getter to get the new secret key.
     *
     * @return New secret key.
     */
    public String getNewSecretKey() {

        return newSecretKey;
    }

    /**
     * Setter to set the new secret key.
     *
     * @param newSecretKey New secret key.
     */
    public void setNewSecretKey(String newSecretKey) {

        this.newSecretKey = newSecretKey;
    }

    /**
     * Getter to get the old IS home path.
     *
     * @return Old IS home path.
     */
    public String getOldISHome() {

        return oldISHome;
    }

    /**
     * Setter to set the old IS home path.
     *
     * @param oldISHome Old IS home path.
     */
    public void setOldISHome(String oldISHome) {

        this.oldISHome = oldISHome;
    }

    /**
     * Getter to get the new IS home path.
     *
     * @return New IS home path.
     */
    public String getNewISHome() {

        return newISHome;
    }

    /**
     * Setter to set the new IS home path.
     *
     * @param newISHome New IS home path.
     */
    public void setNewISHome(String newISHome) {

        this.newISHome = newISHome;
    }

    /**
     * Getter to get the identity database URL.
     *
     * @return identity database URL.
     */
    public String getIdnDBUrl() {

        return idnDBUrl;
    }

    /**
     * Setter to set the identity database URL.
     *
     * @param idnDBUrl Identity database URL.
     */
    public void setIdnDBUrl(String idnDBUrl) {

        this.idnDBUrl = idnDBUrl;
    }

    /**
     * Getter to get the identity database username.
     *
     * @return Identity database username.
     */
    public String getIdnUsername() {

        return idnUsername;
    }

    /**
     * Setter to set the identity database username.
     *
     * @param idnUsername Identity database username.
     */
    public void setIdnUsername(String idnUsername) {

        this.idnUsername = idnUsername;
    }

    /**
     * Getter to get the identity database password.
     *
     * @return Base64 decoded identity database password.
     */
    public String getIdnPassword() {

        return new String(Base64.decode(idnPassword));
    }

    /**
     * Setter to set the identity database password.
     *
     * @param password Identity database password.
     */
    public void setIdnPassword(String password) {

        this.idnPassword = password;
    }

    /**
     * Getter to get the registry database URL.
     *
     * @return Registry database URL.
     */
    public String getRegDBUrl() {

        return regDBUrl;
    }

    /**
     * Setter to set the registry database URL.
     *
     * @param regDBUrl Registry database URL.
     */
    public void setRegDBUrl(String regDBUrl) {

        this.regDBUrl = regDBUrl;
    }

    /**
     * Getter to get the registry database username.
     *
     * @return Registry database username.
     */
    public String getRegUsername() {

        return regUsername;
    }

    /**
     * Setter to set the registry database username.
     *
     * @param regUsername Registry database username.
     */
    public void setRegUsername(String regUsername) {

        this.regUsername = regUsername;
    }

    /**
     * Getter to get the registry database password.
     *
     * @return Registry database password.
     */
    public String getRegPassword() {

        return new String(Base64.decode(regPassword));
    }

    /**
     * Setter to set the registry database password.
     *
     * @param regPassword Registry database password.
     */
    public void setRegPassword(String regPassword) {

        this.regPassword = regPassword;
    }

    /**
     * Getter to get the enable database migrator property value.
     *
     * @return Enable database migrator property value.
     */
    public String getEnableDBMigrator() {

        return enableDBMigrator;
    }

    /**
     * Setter to set the enable database migrator property value.
     *
     * @param enableDBMigrator Enable database migrator property value.
     */
    public void setEnableDBMigrator(String enableDBMigrator) {

        this.enableDBMigrator = enableDBMigrator;
    }

    /**
     * Getter to get the enable configuration file migrator property value.
     *
     * @return Enable configuration file migrator property value.
     */
    public String getEnableConfigMigrator() {

        return enableConfigMigrator;
    }

    /**
     * Setter to set the enable configuration file migrator property value.
     *
     * @param enableConfigMigrator Enable configuration file migrator property value.
     */
    public void setEnableConfigMigrator(String enableConfigMigrator) {

        this.enableConfigMigrator = enableConfigMigrator;
    }

    /**
     * Getter to get the enable syncing migrator property value.
     *
     * @return Enable syncing migrator property value.
     */
    public String getEnableSyncMigrator() {

        return enableSyncMigrator;
    }

    /**
     * Setter to set the enable syncing migrator property value.
     *
     * @param enableSyncMigrator Enable syncing migrator property value.
     */
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
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
    private String newISHome;
    private String oldIdnDBUrl;
    private String oldIdnUsername;
    private String oldIdnPassword;
    private String newIdnDBUrl;
    private String newIdnUsername;
    private String newIdnPassword;
    private String newRegDBUrl;
    private String newRegUsername;
    private String newRegPassword;
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

        String propertiesFilePath = Paths.get("components", "org.wso2.carbon.identity.keyrotation", "src",
                "main", "resources", "properties.yaml").toString();
        if (args.length > 0) {
            propertiesFilePath = args[0];
        }
        File file = new File(propertiesFilePath);
        if (!file.exists()) {
            throw new KeyRotationException(
                    "Error occurred, properties.yaml file not found in provided path, " + propertiesFilePath);
        }
        log.info("Loading Key Rotation Configs from path: " + propertiesFilePath);
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
                if ("oldISHome".equals(props[i].getName()) || "newISHome".equals(props[i].getName())) {
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
     * Getter to get the old IS identity database URL.
     *
     * @return Old IS identity database URL.
     */
    public String getOldIdnDBUrl() {

        return oldIdnDBUrl;
    }

    /**
     * Setter to set the old IS identity database URL.
     *
     * @param oldIdnDBUrl Old IS identity database URL.
     */
    public void setOldIdnDBUrl(String oldIdnDBUrl) {

        this.oldIdnDBUrl = oldIdnDBUrl;
    }

    /**
     * Getter to get the old IS identity database username.
     *
     * @return Old IS identity database username.
     */
    public String getOldIdnUsername() {

        return oldIdnUsername;
    }

    /**
     * Setter to set the old IS identity database username.
     *
     * @param oldIdnUsername Old IS identity database username.
     */
    public void setOldIdnUsername(String oldIdnUsername) {

        this.oldIdnUsername = oldIdnUsername;
    }

    /**
     * Getter to get the old IS identity database password.
     *
     * @return Base64 decoded old IS identity database password.
     */
    public String getOldIdnPassword() {

        return new String(Base64.decode(oldIdnPassword));
    }

    /**
     * Setter to set the old IS identity database password.
     *
     * @param oldIdnPassword Old IS identity database password.
     */
    public void setOldIdnPassword(String oldIdnPassword) {

        this.oldIdnPassword = oldIdnPassword;
    }

    /**
     * Getter to get the new IS identity database URL.
     *
     * @return New IS identity database URL.
     */
    public String getNewIdnDBUrl() {

        return newIdnDBUrl;
    }

    /**
     * Setter to set the new IS identity database URL.
     *
     * @param newIdnDBUrl New IS identity database URL.
     */
    public void setNewIdnDBUrl(String newIdnDBUrl) {

        this.newIdnDBUrl = newIdnDBUrl;
    }

    /**
     * Getter to get the new IS identity database username.
     *
     * @return New IS identity database username.
     */
    public String getNewIdnUsername() {

        return newIdnUsername;
    }

    /**
     * Setter to set the new IS identity database username.
     *
     * @param newIdnUsername New IS identity database username.
     */
    public void setNewIdnUsername(String newIdnUsername) {

        this.newIdnUsername = newIdnUsername;
    }

    /**
     * Getter to get the new IS identity database password.
     *
     * @return Base64 decoded new IS identity database password.
     */
    public String getNewIdnPassword() {

        return new String(Base64.decode(newIdnPassword));
    }

    /**
     * Setter to set the new IS identity database password.
     *
     * @param password New IS identity database password.
     */
    public void setNewIdnPassword(String password) {

        this.newIdnPassword = password;
    }

    /**
     * Getter to get the registry database URL.
     *
     * @return Registry database URL.
     */
    public String getNewRegDBUrl() {

        return newRegDBUrl;
    }

    /**
     * Setter to set the registry database URL.
     *
     * @param newRegDBUrl Registry database URL.
     */
    public void setNewRegDBUrl(String newRegDBUrl) {

        this.newRegDBUrl = newRegDBUrl;
    }

    /**
     * Getter to get the registry database username.
     *
     * @return Registry database username.
     */
    public String getNewRegUsername() {

        return newRegUsername;
    }

    /**
     * Setter to set the registry database username.
     *
     * @param newRegUsername Registry database username.
     */
    public void setNewRegUsername(String newRegUsername) {

        this.newRegUsername = newRegUsername;
    }

    /**
     * Getter to get the registry database password.
     *
     * @return Registry database password.
     */
    public String getNewRegPassword() {

        return new String(Base64.decode(newRegPassword));
    }

    /**
     * Setter to set the registry database password.
     *
     * @param newRegPassword Registry database password.
     */
    public void setNewRegPassword(String newRegPassword) {

        this.newRegPassword = newRegPassword;
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

        return "\noldSecretKey: " + oldSecretKey + "\nnewSecretKey: " + newSecretKey + "\nnewISHome: " + newISHome +
                "\noldIdnDBUrl: " + oldIdnDBUrl + "\noldIdnUsername: " + oldIdnUsername + "\noldIdnPassword: " +
                oldIdnPassword + "\nnewIdnDBUrl: " + newIdnDBUrl + "\nnewIdnUsername: " + newIdnUsername +
                "\nnewIdnPassword: " + newIdnPassword + "\nnewRegDBUrl: " + newRegDBUrl + "\nnewRegUsername: " +
                newRegUsername + "\nnewRegPassword: " + newRegPassword + "\nenableDBMigrator: " + enableDBMigrator +
                "\nenableConfigMigrator: " + enableConfigMigrator + "\nenableSyncMigrator: " + enableSyncMigrator +
                "\n";
    }
}

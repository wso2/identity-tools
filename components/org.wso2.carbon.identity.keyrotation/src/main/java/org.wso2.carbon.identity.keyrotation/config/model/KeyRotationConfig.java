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

package org.wso2.carbon.identity.keyrotation.config.model;

import org.apache.axiom.om.util.Base64;

/**
 * This class holds implementations needed to load the configurations in the properties.yaml file.
 */
public class KeyRotationConfig {

    private static final KeyRotationConfig instance = new KeyRotationConfig();
    private String oldSecretKey;
    private String newSecretKey;
    private String newISHome;
    private String idnDBUrl;
    private String idnUsername;
    private String idnPassword;
    private String regDBUrl;
    private String regUsername;
    private String regPassword;
    private int chunkSize;
    private boolean enableDBMigrator;
    private boolean enableConfigMigrator;

    public static KeyRotationConfig getInstance() {

        return instance;
    }

    /**
     * Get for the old secret key.
     *
     * @return Old secret key.
     */
    public String getOldSecretKey() {

        return oldSecretKey;
    }

    /**
     * Set for the old secret key.
     *
     * @param oldSecretKey Old secret key.
     */
    public void setOldSecretKey(String oldSecretKey) {

        this.oldSecretKey = oldSecretKey;
    }

    /**
     * Get for the new secret key.
     *
     * @return New secret key.
     */
    public String getNewSecretKey() {

        return newSecretKey;
    }

    /**
     * Set for the new secret key.
     *
     * @param newSecretKey New secret key.
     */
    public void setNewSecretKey(String newSecretKey) {

        this.newSecretKey = newSecretKey;
    }

    /**
     * Get for the new IS home path.
     *
     * @return New IS home path.
     */
    public String getNewISHome() {

        return newISHome;
    }

    /**
     * Set for the new IS home path.
     *
     * @param newISHome New IS home path.
     */
    public void setNewISHome(String newISHome) {

        this.newISHome = newISHome;
    }

    /**
     * Get for the new IS identity database URL.
     *
     * @return New IS identity database URL.
     */
    public String getIdnDBUrl() {

        return idnDBUrl;
    }

    /**
     * Set for the new IS identity database URL.
     *
     * @param idnDBUrl New IS identity database URL.
     */
    public void setIdnDBUrl(String idnDBUrl) {

        this.idnDBUrl = idnDBUrl;
    }

    /**
     * Get for the new IS identity database username.
     *
     * @return New IS identity database username.
     */
    public String getIdnUsername() {

        return idnUsername;
    }

    /**
     * Set for the new IS identity database username.
     *
     * @param idnUsername New IS identity database username.
     */
    public void setIdnUsername(String idnUsername) {

        this.idnUsername = idnUsername;
    }

    /**
     * Get for the new IS identity database password.
     *
     * @return Base64 decoded new IS identity database password.
     */
    public String getIdnPassword() {

        return new String(Base64.decode(idnPassword));
    }

    /**
     * Set for the new IS identity database password.
     *
     * @param password New IS identity database password.
     */
    public void setIdnPassword(String password) {

        this.idnPassword = password;
    }

    /**
     * Get for the registry database URL.
     *
     * @return Registry database URL.
     */
    public String getRegDBUrl() {

        return regDBUrl;
    }

    /**
     * Set for the registry database URL.
     *
     * @param regDBUrl Registry database URL.
     */
    public void setRegDBUrl(String regDBUrl) {

        this.regDBUrl = regDBUrl;
    }

    /**
     * Get for the registry database username.
     *
     * @return Registry database username.
     */
    public String getRegUsername() {

        return regUsername;
    }

    /**
     * Set for the registry database username.
     *
     * @param regUsername Registry database username.
     */
    public void setRegUsername(String regUsername) {

        this.regUsername = regUsername;
    }

    /**
     * Get for the registry database password.
     *
     * @return Registry database password.
     */
    public String getRegPassword() {

        return new String(Base64.decode(regPassword));
    }

    /**
     * Set for the registry database password.
     *
     * @param regPassword Registry database password.
     */
    public void setRegPassword(String regPassword) {

        this.regPassword = regPassword;
    }

    /**
     * Return the chunk size for the records retrieved from the database.
     *
     * @return chunk size.
     */
    public int getChunkSize() {

        return chunkSize;
    }

    /**
     * Set the chunk size for the records retrieved from the database.
     *
     * @param chunkSize Chunk size.
     */
    public void setChunkSize(int chunkSize) {

        this.chunkSize = chunkSize;
    }

    /**
     * Get for the enable database migrator property value.
     *
     * @return Enable database migrator property value.
     */
    public boolean getEnableDBMigrator() {

        return enableDBMigrator;
    }

    /**
     * Set for the enable database migrator property value.
     *
     * @param enableDBMigrator Enable database migrator property value.
     */
    public void setEnableDBMigrator(boolean enableDBMigrator) {

        this.enableDBMigrator = enableDBMigrator;
    }

    /**
     * Get for the enable configuration file migrator property value.
     *
     * @return Enable configuration file migrator property value.
     */
    public boolean getEnableConfigMigrator() {

        return enableConfigMigrator;
    }

    /**
     * Set for the enable configuration file migrator property value.
     *
     * @param enableConfigMigrator Enable configuration file migrator property value.
     */
    public void setEnableConfigMigrator(boolean enableConfigMigrator) {

        this.enableConfigMigrator = enableConfigMigrator;
    }
}

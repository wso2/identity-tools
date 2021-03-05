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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationServiceUtils;

import static org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants.PROPERTIES_FILE_PATH;

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
    private static KeyRotationConfig keyRotationConfig = null;
    private static final Log log = LogFactory.getLog(KeyRotationConfig.class);

    public static KeyRotationConfig loadConfigs() throws KeyRotationException {

        if (keyRotationConfig == null) {
            if (log.isDebugEnabled()) {
                log.info("Loading Key Rotation Configs from absolute path: " + PROPERTIES_FILE_PATH);
            }
            keyRotationConfig = KeyRotationServiceUtils.loadKeyRotationConfig(PROPERTIES_FILE_PATH);
            if (log.isDebugEnabled()) {
                log.info("Successfully loaded the config file.");
            }
        }
        return keyRotationConfig;
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

        return idnPassword;
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

        return regPassword;
    }

    public void setRegPassword(String regPassword) {

        this.regPassword = regPassword;
    }

    @Override
    public String toString() {

        return "\noldSecretKey: " + oldSecretKey + "\nnewSecretKey: " + newSecretKey + "\noldISHome: " + oldISHome +
                "\nnewISHome: " + newISHome + "\nidnDBUrl: " + idnDBUrl + "\nidnUsername: " + idnUsername +
                "\nidnPassword: " + idnPassword + "\nregDBUrl: " + regDBUrl + "\nregUsername: " + regUsername +
                "\nregPassword: " + regPassword + "\n";
    }
}

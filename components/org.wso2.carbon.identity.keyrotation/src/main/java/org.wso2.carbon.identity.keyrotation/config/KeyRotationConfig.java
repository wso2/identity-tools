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
import org.wso2.carbon.identity.keyrotation.util.KeyRotationServiceUtils;

import static org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants.PROPERTIES_FILE_PATH;

/**
 * Class to map the properties.yaml file content.
 */
public class KeyRotationConfig {

    private String oldSecretKey;
    private String newSecretKey;
    private String isHome;
    private static KeyRotationConfig keyRotationConfig = null;
    private static final Log log = LogFactory.getLog(KeyRotationConfig.class);

    public static KeyRotationConfig loadConfigs() {

        if (keyRotationConfig == null) {
            String keyRotationConfigFileName = PROPERTIES_FILE_PATH;
            log.info("Loading Key Rotation Configs, PATH:" + PROPERTIES_FILE_PATH);
            try {
                keyRotationConfig = KeyRotationServiceUtils.loadKeyRotationConfig(keyRotationConfigFileName);
            } catch (Exception e) {
                log.error("Error while loading Key Rotation configs.", e);
            }
            log.info("Successfully loaded the config file.");
        }

        return KeyRotationConfig.keyRotationConfig;

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

    public String getIsHome() {

        return isHome;
    }

    public void setIsHome(String isHome) {

        this.isHome = isHome;
    }

    @Override
    public String toString() {

        return "\noldSecretKey: " + oldSecretKey + "\nnewSecretKey: " + newSecretKey + "\nisHome: " + isHome + "\n";
    }
}

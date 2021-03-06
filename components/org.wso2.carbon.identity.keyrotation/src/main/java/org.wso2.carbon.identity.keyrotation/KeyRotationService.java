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

package org.wso2.carbon.identity.keyrotation;

import org.wso2.carbon.identity.keyrotation.config.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.service.ConfigFileKeyRotator;
import org.wso2.carbon.identity.keyrotation.service.DBKeyRotator;
import org.wso2.carbon.identity.keyrotation.service.SyncedDataKeyRotator;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

/**
 * This class holds the symmetric key rotation service.
 */
public class KeyRotationService {

    public static void main(String[] args) throws KeyRotationException {

        KeyRotationConfig configs = KeyRotationConfig.getInstance().loadConfigs(args);
        if (Boolean.parseBoolean(configs.getEnableDBMigrator())) {
            DBKeyRotator.getInstance().dbReEncryptor(configs);
        }
        if (Boolean.parseBoolean(configs.getEnableConfigMigrator())) {
            ConfigFileKeyRotator.getInstance().configFileReEncryptor(configs);
        }
        if (Boolean.parseBoolean(configs.getEnableSyncMigrator())) {
            SyncedDataKeyRotator.getInstance().syncedDataReEncryptor(configs);
        }
    }
}

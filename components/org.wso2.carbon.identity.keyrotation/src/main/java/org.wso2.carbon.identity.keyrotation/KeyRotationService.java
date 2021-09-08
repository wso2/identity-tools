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

import org.wso2.carbon.identity.keyrotation.config.FileBasedKeyRotationConfigProvider;
import org.wso2.carbon.identity.keyrotation.config.KeyRotationConfigProvider;
import org.wso2.carbon.identity.keyrotation.config.model.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.service.ConfigFileKeyRotator;
import org.wso2.carbon.identity.keyrotation.service.DBKeyRotator;
import org.wso2.carbon.identity.keyrotation.service.SyncedDataKeyRotator;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class holds the symmetric key rotation service.
 */
public class KeyRotationService {

    public static void main(String[] args) throws KeyRotationException {

        String propertiesFilePath;
        if (new File("properties.yaml").isFile()) {
            propertiesFilePath = "properties.yaml";
        } else {
            propertiesFilePath = Paths.get("components", "org.wso2.carbon.identity.keyrotation", "src",
                    "main", "resources", "properties.yaml").toString();
        }
        KeyRotationConfigProvider configProvider = null;
        try {
            configProvider =
                    new FileBasedKeyRotationConfigProvider(Files.newInputStream(Paths.get(propertiesFilePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        initService(configProvider.getKeyRotationConfig());
    }

    private static void initService(KeyRotationConfig config) throws KeyRotationException {

        if (Boolean.parseBoolean(config.getEnableDBMigrator())) {
            DBKeyRotator.getInstance().dbReEncryptor(config);
        }
        if (Boolean.parseBoolean(config.getEnableConfigMigrator())) {
            ConfigFileKeyRotator.getInstance().configFileReEncryptor(config);
        }
        if (Boolean.parseBoolean(config.getEnableSyncMigrator())) {
            SyncedDataKeyRotator.getInstance().syncedDataReEncryptor(config);
        }
    }
}

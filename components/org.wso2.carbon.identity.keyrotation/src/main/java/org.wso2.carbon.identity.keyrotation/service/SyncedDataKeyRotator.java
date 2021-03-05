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

/**
 * Synced data reEncryption service.
 */
public class SyncedDataKeyRotator {

    private static final Log log = LogFactory.getLog(SyncedDataKeyRotator.class);
    private static final SyncedDataKeyRotator instance = new SyncedDataKeyRotator();

    public static SyncedDataKeyRotator getInstance() {

        return instance;
    }

    /**
     * ReEncryption of the synced data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public void syncedDataReEncryptor(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.info("Re-encrypting synced data...");
        log.info("Re-encrypting synced data completed...\n");
    }
}

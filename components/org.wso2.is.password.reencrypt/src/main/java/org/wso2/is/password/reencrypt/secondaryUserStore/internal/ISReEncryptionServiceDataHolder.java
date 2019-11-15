/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.is.password.reencrypt.secondaryUserStore.internal;

import org.wso2.carbon.base.api.ServerConfigurationService;

/**
 * ReEncryption Service Data Holder
 */
public class ISReEncryptionServiceDataHolder {

    private static volatile ISReEncryptionServiceDataHolder isReEncryptionServiceDataHolder =
            new ISReEncryptionServiceDataHolder();
    private ServerConfigurationService serverConfigurationService;

    private ISReEncryptionServiceDataHolder() {

    }

    public static ISReEncryptionServiceDataHolder getInstance() {

        return isReEncryptionServiceDataHolder;
    }

    public ServerConfigurationService getServerConfigurationService() {

        return serverConfigurationService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {

        this.serverConfigurationService = serverConfigurationService;
    }
}

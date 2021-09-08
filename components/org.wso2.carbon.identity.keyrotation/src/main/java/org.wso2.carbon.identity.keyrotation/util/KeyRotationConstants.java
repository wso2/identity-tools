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

package org.wso2.carbon.identity.keyrotation.util;

/**
 * This class holds the constants needed for the key rotation functionality.
 */
public class KeyRotationConstants {


    public static final String OLD_SECRET_KEY = "oldSecretKey";
    public static final String OLD_IDN_DB_URL = "oldIdnDBUrl";
    public static final String OLD_IDN_USERNAME = "oldIdnUsername";
    public static final String OLD_IDN_PASSWORD = "oldIdnPassword";

    public static final String NEW_SECRET_KEY = "newSecretKey";
    public static final String NEW_IS_HOME = "newISHome";
    public static final String NEW_IDN_DB_URL = "newIdnDBUrl";
    public static final String NEW_IDN_USERNAME = "newIdnUsername";
    public static final String NEW_IDN_PASSWORD = "newIdnPassword";
    public static final String NEW_REG_DB_URL = "newRegDBUrl";
    public static final String NEW_REG_USERNAME = "newRegUsername";
    public static final String NEW_REG_PASSWORD = "newRegPassword";
    public static final String ENABLE_DB_MIGRATOR = "enableDBMigrator";
    public static final String ENABLE_CONFIG_MIGRATOR = "enableConfigMigrator";
    public static final String ENABLE_SYNC_MIGRATOR = "enableSyncMigrator";

    public static final String ALGORITHM = "AES";
    public static final String TRANSFORMATION = "AES/GCM/NoPadding";
    public static final String USERSTORES = "userstores";
    public static final String REPOSITORY = "repository";
    public static final String TENANTS = "tenants";
    public static final String DEPLOYMENT = "deployment";
    public static final String SERVER = "server";
    public static final String EVENT_PUBLISHERS = "eventpublishers";
    public static final String EVENT_PUBLISHER = "EVENT_PUBLISHER";
    public static final String SUPER_TENANT = "SUPER_TENANT";
    public static final String TENANT = "TENANT";
}

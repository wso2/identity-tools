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

package org.wso2.carbon.identity.keyrotation.util;

/**
 * This class holds the constants needed for the key rotation functionality.
 */
public class KeyRotationConstants {

    public static final String PROPERTY_FILE_NAME = "properties.yaml";
    public static final String DEFAULT_PROPERTY_FILE_PATH = "components/org.wso2.carbon.identity" +
            ".keyrotation/src/main/resources/properties.yaml";

    public static final String OLD_SECRET_KEY = "oldSecretKey";
    public static final String NEW_SECRET_KEY = "newSecretKey";
    public static final String IS_HOME = "ishome";
    public static final String IDN_DB_URL = "idnDBUrl";
    public static final String IDN_USERNAME = "idnUsername";
    public static final String IDN_PASSWORD = "idnPassword";
    public static final String REG_DB_URL = "regDBUrl";
    public static final String REG_USERNAME = "regUsername";
    public static final String REG_PASSWORD = "regPassword";
    public static final String CHUNK_SIZE = "chunkSize";
    public static final String ENABLE_DB_MIGRATOR = "enableDBMigrator";
    public static final String ENABLE_CONFIG_MIGRATOR = "enableConfigMigrator";
    public static final String ENABLE_WORKFLOW_MIGRATOR = "enableWorkFlowMigrator";

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

    public static final String PROFILE_NAME = "PROFILE_NAME";
    public static final String USERNAME = "USERNAME";
    public static final String TENANT_ID = "TENANT_ID";
    public static final String PASSWORD = "PASSWORD";

    public static final String USER_NAME = "USER_NAME";
    public static final String DATA_KEY = "DATA_KEY";
    public static final String DATA_VALUE = "DATA_VALUE";

    public static final String CODE_ID = "CODE_ID";
    public static final String AUTHORIZATION_CODE = "AUTHORIZATION_CODE";
    public static final String CONSUMER_KEY_ID = "CONSUMER_KEY_ID";
    public static final String TOKEN_ID = "TOKEN_ID";
    public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String REFRESH_TOKEN = "REFRESH_TOKEN";
    public static final String ID = "ID";
    public static final String CONSUMER_SECRET = "CONSUMER_SECRET";
    public static final String APP_NAME = "APP_NAME";

    public static final String REG_ID = "REG_ID";
    public static final String REG_NAME = "REG_NAME";
    public static final String REG_VALUE = "REG_VALUE";
    public static final String REG_TENANT_ID = "REG_TENANT_ID";

    public static final String USERSTORE_PROPERTY = "Property";
    public static final String PUBLISHER_PROPERTY = "property";

    public static final int GCM_IV_LENGTH = 16;
    public static final String JAVA_SECURITY_API_PROVIDER = "BC";

    public static final String REGISTRY_PASSWORD = "password";
    public static final String PRIVATE_KEY_PASS = "privatekeyPass";
    public static final String SUBSCRIBER_PASSWORD = "subscriberPassword";

    public static final String SECRET_VALUE = "SECRET_VALUE";
    public static final String PROPERTY_ID = "PROPERTY_ID";
    public static final String PROPERTY_VALUE = "PROPERTY_VALUE";
    public static final String SUBSCRIBER_ID = "SUBSCRIBER_ID";
}

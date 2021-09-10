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

    public static final String PROPERTY_FILE_NAME = "properties.yaml";
    public static final String DEFAULT_PROPERTY_FILE_PATH = "components/org.wso2.carbon.identity" +
            ".keyrotation/src/main/resources/properties.yaml";

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
    public static final String CHUNK_SIZE = "chunkSize";
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

    public static final String PROFILE_NAME = "PROFILE_NAME";
    public static final String USERNAME = "USERNAME";
    public static final String TENANT_ID = "TENANT_ID";
    public static final String PASSWORD = "PASSWORD";

    public static final String USER_NAME = "USER_NAME";
    public static final String DATA_KEY = "DATA_KEY";
    public static final String DATA_VALUE = "DATA_VALUE";
    public static final String AVAILABILITY = "AVAILABILITY";
    public static final String SYNC_ID = "SYNC_ID";
    public static final String SYNCED = "SYNCED";

    public static final String CODE_ID = "CODE_ID";
    public static final String AUTHORIZATION_CODE = "AUTHORIZATION_CODE";
    public static final String CONSUMER_KEY_ID = "CONSUMER_KEY_ID";
    public static final String CALLBACK_URL = "CALLBACK_URL";
    public static final String SCOPE = "SCOPE";
    public static final String AUTHZ_USER = "AUTHZ_USER";
    public static final String USER_DOMAIN = "USER_DOMAIN";
    public static final String TIME_CREATED = "TIME_CREATED";
    public static final String VALIDITY_PERIOD = "VALIDITY_PERIOD";
    public static final String STATE = "STATE";
    public static final String SUBJECT_IDENTIFIER = "SUBJECT_IDENTIFIER";
    public static final String PKCE_CODE_CHALLENGE = "PKCE_CODE_CHALLENGE";
    public static final String PKCE_CODE_CHALLENGE_METHOD = "PKCE_CODE_CHALLENGE_METHOD";
    public static final String AUTHORIZATION_CODE_HASH = "AUTHORIZATION_CODE_HASH";
    public static final String IDP_ID = "IDP_ID";
    public static final String TOKEN_ID = "TOKEN_ID";
    public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String REFRESH_TOKEN = "REFRESH_TOKEN";
    public static final String ID = "ID";
    public static final String CONSUMER_SECRET = "CONSUMER_SECRET";
    public static final String APP_NAME = "APP_NAME";
    public static final String TOKEN_SCOPE = "TOKEN_SCOPE";
    public static final String USER_TYPE = "USER_TYPE";
    public static final String GRANT_TYPE = "GRANT_TYPE";
    public static final String REFRESH_TOKEN_TIME_CREATED = "REFRESH_TOKEN_TIME_CREATED";
    public static final String REFRESH_TOKEN_VALIDITY_PERIOD = "REFRESH_TOKEN_VALIDITY_PERIOD";
    public static final String TOKEN_SCOPE_HASH = "TOKEN_SCOPE_HASH";
    public static final String TOKEN_STATE = "TOKEN_STATE";
    public static final String TOKEN_STATE_ID = "TOKEN_STATE_ID";
    public static final String ACCESS_TOKEN_HASH = "ACCESS_TOKEN_HASH";
    public static final String REFRESH_TOKEN_HASH = "REFRESH_TOKEN_HASH";
    public static final String TOKEN_BINDING_REF = "TOKEN_BINDING_REF";

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
}

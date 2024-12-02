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

package org.wso2.carbon.identity.keyrotation.dao;

/**
 * This class holds DB related constants.
 */
public class DBConstants {

    public static final int DEFAULT_CHUNK_SIZE = 2;
    public static final String REQUEST = "REQUEST";
    public static final String POSTGRESQL = "PostgreSQL";
    public static final String MSSQL = "SQL Server";
    public static final String ORACLE = "Oracle";
    public static final String SECRET_KEY = "http://wso2.org/claims/identity/secretkey";
    public static final String VERIFIED_SECRET_KEY = "http://wso2.org/claims/identity/verifySecretkey";
    public static final String UPDATED_AT = "UPDATED_AT";

    // ***************************** IDN_IDENTITY_USER_DATA ******************************************
    public static final String GET_TOTP_SECRET = "SELECT TENANT_ID, USER_NAME, DATA_KEY, DATA_VALUE " +
            "FROM IDN_IDENTITY_USER_DATA WHERE DATA_KEY=? OR DATA_KEY=? ORDER BY TENANT_ID, USER_NAME, DATA_KEY LIMIT" +
            " ?, ?";
    public static final String GET_TOTP_SECRET_POSTGRE = "SELECT TENANT_ID, USER_NAME, DATA_VALUE, DATA_KEY FROM " +
            "IDN_IDENTITY_USER_DATA WHERE DATA_KEY=? OR DATA_KEY=? ORDER BY TENANT_ID, USER_NAME, DATA_KEY LIMIT ? " +
            "OFFSET ?";
    public static final String GET_TOTP_SECRET_OTHER = "SELECT TENANT_ID, USER_NAME, DATA_VALUE, DATA_KEY FROM " +
            "IDN_IDENTITY_USER_DATA WHERE DATA_KEY=? OR DATA_KEY=? ORDER BY TENANT_ID, USER_NAME, DATA_KEY OFFSET ? " +
            "ROWS FETCH NEXT ? ROWS ONLY";
    public static final String UPDATE_TOTP_SECRET =
            "UPDATE IDN_IDENTITY_USER_DATA SET DATA_VALUE=? WHERE TENANT_ID=? AND USER_NAME=? AND DATA_KEY=? " +
                    "AND DATA_VALUE=?";

    // ***************************** IDN_OAUTH2_AUTHORIZATION_CODE ******************************************
    public static final String GET_OAUTH_AUTHORIZATION_CODE =
            "SELECT CODE_ID, AUTHORIZATION_CODE, CONSUMER_KEY_ID FROM IDN_OAUTH2_AUTHORIZATION_CODE " +
                    "ORDER BY CODE_ID LIMIT ?, ?";
    public static final String GET_OAUTH_AUTHORIZATION_CODE_POSTGRE =
            "SELECT CODE_ID, AUTHORIZATION_CODE, CONSUMER_KEY_ID FROM IDN_OAUTH2_AUTHORIZATION_CODE ORDER BY " +
                    "CODE_ID LIMIT ? OFFSET ?";
    public static final String GET_OAUTH_AUTHORIZATION_CODE_OTHER =
            "SELECT CODE_ID, AUTHORIZATION_CODE, CONSUMER_KEY_ID FROM IDN_OAUTH2_AUTHORIZATION_CODE ORDER BY " +
                    "CODE_ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    public static final String UPDATE_OAUTH_AUTHORIZATION_CODE =
            "UPDATE IDN_OAUTH2_AUTHORIZATION_CODE SET AUTHORIZATION_CODE=? WHERE CODE_ID=? AND AUTHORIZATION_CODE=?";

    // ***************************** IDN_OAUTH2_ACCESS_TOKEN ******************************************
    public static final String GET_OAUTH_ACCESS_TOKEN = "SELECT TOKEN_ID, ACCESS_TOKEN, REFRESH_TOKEN, " +
            "CONSUMER_KEY_ID FROM IDN_OAUTH2_ACCESS_TOKEN ORDER BY TOKEN_ID LIMIT ?, ?";
    public static final String GET_OAUTH_ACCESS_TOKEN_POSTGRE = "SELECT TOKEN_ID, ACCESS_TOKEN, REFRESH_TOKEN, " +
            "CONSUMER_KEY_ID FROM IDN_OAUTH2_ACCESS_TOKEN ORDER BY TOKEN_ID LIMIT ? OFFSET ?";
    public static final String GET_OAUTH_ACCESS_TOKEN_OTHER = "SELECT TOKEN_ID, ACCESS_TOKEN, REFRESH_TOKEN, " +
            "CONSUMER_KEY_ID FROM IDN_OAUTH2_ACCESS_TOKEN ORDER BY TOKEN_ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    public static final String UPDATE_OAUTH_ACCESS_TOKEN =
            "UPDATE IDN_OAUTH2_ACCESS_TOKEN SET ACCESS_TOKEN=?, REFRESH_TOKEN=? WHERE TOKEN_ID=? AND ACCESS_TOKEN=? " +
                    "AND REFRESH_TOKEN=?";

    // ***************************** IDN_OAUTH_CONSUMER_APPS ******************************************
    public static final String GET_OAUTH_SECRET = "SELECT ID, CONSUMER_SECRET, APP_NAME " +
            "FROM IDN_OAUTH_CONSUMER_APPS ORDER BY ID LIMIT ?, ?";
    public static final String GET_OAUTH_SECRET_POSTGRE = "SELECT ID, CONSUMER_SECRET, APP_NAME " +
            "FROM IDN_OAUTH_CONSUMER_APPS ORDER BY ID LIMIT ? OFFSET ?";
    public static final String GET_OAUTH_SECRET_OTHER = "SELECT ID, CONSUMER_SECRET, APP_NAME " +
            "FROM IDN_OAUTH_CONSUMER_APPS ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    public static final String UPDATE_OAUTH_SECRET = "UPDATE IDN_OAUTH_CONSUMER_APPS SET CONSUMER_SECRET=? " +
            "WHERE ID=? AND CONSUMER_SECRET=?";

    // ***************************** WF_BPS_PROFILE Deprecated from IS 7 ******************************************
    public static final String GET_BPS_PASSWORD = "SELECT PROFILE_NAME, USERNAME, TENANT_ID, PASSWORD " +
            "FROM WF_BPS_PROFILE ORDER BY PROFILE_NAME, TENANT_ID LIMIT ?, ?";
    public static final String GET_BPS_PASSWORD_POSTGRE = "SELECT PROFILE_NAME, USERNAME, TENANT_ID, PASSWORD " +
            "FROM WF_BPS_PROFILE ORDER BY PROFILE_NAME, TENANT_ID LIMIT ? OFFSET ?";
    public static final String GET_BPS_PASSWORD_OTHER = "SELECT PROFILE_NAME, USERNAME, TENANT_ID, PASSWORD " +
            "FROM WF_BPS_PROFILE ORDER BY PROFILE_NAME, TENANT_ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    public static final String UPDATE_BPS_PASSWORD = "UPDATE WF_BPS_PROFILE SET PASSWORD=? WHERE PROFILE_NAME=? AND " +
            "TENANT_ID=? AND PASSWORD=?";

    // ***************************** WF_REQUEST Deprecated from IS 7 ******************************************
    public static final String GET_WF_REQUEST =
            "SELECT UUID, REQUEST, UPDATED_AT FROM WF_REQUEST ORDER BY UUID LIMIT ?, ?";
    public static final String GET_WF_REQUEST_POSTGRE =
            "SELECT UUID, REQUEST, UPDATED_AT FROM WF_REQUEST ORDER BY UUID LIMIT ? " +
                    "OFFSET ?";
    public static final String GET_WF_REQUEST_OTHER =
            "SELECT UUID, REQUEST, UPDATED_AT FROM WF_REQUEST ORDER BY UUID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    public static final String UPDATE_WF_REQUEST = "UPDATE WF_REQUEST SET REQUEST=? WHERE UUID=? AND UPDATED_AT=?";

    // ***************************** REG_PROPERTY ******************************************
    public static final String GET_REG_PROPERTY_DATA = "SELECT REG_ID, REG_NAME, REG_VALUE, REG_TENANT_ID " +
            "FROM REG_PROPERTY WHERE REG_NAME=? ORDER BY REG_ID, REG_TENANT_ID LIMIT ?, ?";
    public static final String GET_REG_PROPERTY_DATA_POSTGRE = "SELECT REG_ID, REG_NAME, REG_VALUE, REG_TENANT_ID " +
            "FROM REG_PROPERTY WHERE REG_NAME=? ORDER BY REG_ID, REG_TENANT_ID LIMIT ? OFFSET ?";
    public static final String GET_REG_PROPERTY_DATA_OTHER = "SELECT REG_ID, REG_NAME, REG_VALUE, REG_TENANT_ID " +
            "FROM REG_PROPERTY WHERE REG_NAME=? ORDER BY REG_ID, REG_TENANT_ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    public static final String UPDATE_REG_PROPERTY_DATA =
            "UPDATE REG_PROPERTY SET REG_VALUE=? WHERE REG_ID=? AND REG_TENANT_ID=? AND REG_VALUE=?";

    // ***************************** IDN_SECRET ******************************************
    public static final String GET_IDN_SECRET = "SELECT ID, SECRET_VALUE " +
            "FROM IDN_SECRET ORDER BY ID LIMIT ?, ?";
    public static final String GET_IDN_SECRET_POSTGRE = "SELECT ID, SECRET_VALUE " +
            "FROM IDN_SECRET ORDER BY ID LIMIT ? OFFSET ?";
    public static final String GET_IDN_SECRET_OTHER = "SELECT ID, SECRET_VALUE " +
            "FROM IDN_SECRET ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    public static final String UPDATE_IDN_SECRET = "UPDATE IDN_SECRET SET SECRET_VALUE=? " +
            "WHERE ID=? AND SECRET_VALUE=?";

    // ***************************** IDN_XACML_SUBSCRIBER_PROPERTY ******************************************
    public static final String GET_XACML_SECRET =
            "SELECT PROPERTY_ID, PROPERTY_VALUE, SUBSCRIBER_ID, TENANT_ID FROM IDN_XACML_SUBSCRIBER_PROPERTY " +
                    "WHERE PROPERTY_ID=? AND IS_SECRET=TRUE ORDER BY SUBSCRIBER_ID LIMIT ?, ?";
    public static final String GET_XACML_SECRET_POSTGRE =
            "SELECT PROPERTY_ID, PROPERTY_VALUE, SUBSCRIBER_ID, TENANT_ID FROM IDN_XACML_SUBSCRIBER_PROPERTY " +
                    "WHERE PROPERTY_ID=? ORDER BY SUBSCRIBER_ID LIMIT ? OFFSET ?";
    public static final String GET_XACML_SECRET_OTHER =
            "SELECT PROPERTY_ID, PROPERTY_VALUE, SUBSCRIBER_ID, TENANT_ID FROM IDN_XACML_SUBSCRIBER_PROPERTY " +
                    "WHERE PROPERTY_ID=? ORDER BY SUBSCRIBER_ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    public static final String UPDATE_XACML_SECRET = "UPDATE IDN_XACML_SUBSCRIBER_PROPERTY SET PROPERTY_VALUE=? " +
            "WHERE PROPERTY_ID=? AND SUBSCRIBER_ID=? AND TENANT_ID=? AND PROPERTY_VALUE=?";
}

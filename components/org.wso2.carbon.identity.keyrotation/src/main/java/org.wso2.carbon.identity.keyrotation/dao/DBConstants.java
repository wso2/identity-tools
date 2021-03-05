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
 * Class to store DB related constants.
 */
public class DBConstants {

    public static final int CHUNK_SIZE = 5;
    public static final String TEST_CONSUMER_KEY_ID = "39";
    public static final String TEST_APP_NAME = "dispatch";
    public static final String TEST_USERNAME = "mytest";
    public static final String CREDENTIAL = "Credential";
    public static final String REQUEST = "REQUEST";
    public static final String DATA_KEY = "http://wso2.org/claims/identity/secretkey";
    public static final String GET_TOTP_SECRET = "SELECT TENANT_ID, USER_NAME, DATA_KEY, DATA_VALUE " +
            "FROM IDN_IDENTITY_USER_DATA ORDER BY TENANT_ID, USER_NAME, DATA_KEY LIMIT ?, ?";
    public static final String UPDATE_TOTP_SECRET =
            "UPDATE IDN_IDENTITY_USER_DATA SET DATA_VALUE=? WHERE TENANT_ID=? AND USER_NAME=? AND DATA_KEY=?";
    public static final String GET_OAUTH_AUTHORIZATION_CODE = "SELECT CODE_ID, AUTHORIZATION_CODE, CONSUMER_KEY_ID " +
            "FROM IDN_OAUTH2_AUTHORIZATION_CODE ORDER BY CODE_ID LIMIT ?, ?";
    public static final String UPDATE_OAUTH_AUTHORIZATION_CODE =
            "UPDATE IDN_OAUTH2_AUTHORIZATION_CODE SET AUTHORIZATION_CODE=? WHERE CODE_ID=?";
    public static final String GET_OAUTH_ACCESS_TOKEN = "SELECT TOKEN_ID, ACCESS_TOKEN, CONSUMER_KEY_ID " +
            "FROM IDN_OAUTH2_ACCESS_TOKEN ORDER BY TOKEN_ID LIMIT ?, ?";
    public static final String UPDATE_OAUTH_ACCESS_TOKEN =
            "UPDATE IDN_OAUTH2_ACCESS_TOKEN SET ACCESS_TOKEN=? WHERE TOKEN_ID=?";
    public static final String GET_OAUTH_SECRET = "SELECT ID, CONSUMER_SECRET, APP_NAME " +
            "FROM IDN_OAUTH_CONSUMER_APPS ORDER BY ID LIMIT ?, ?";
    public static final String UPDATE_OAUTH_SECRET = "UPDATE IDN_OAUTH_CONSUMER_APPS SET CONSUMER_SECRET=? WHERE ID=?";
    public static final String GET_BPS_PASSWORD = "SELECT PROFILE_NAME, USERNAME, TENANT_ID, PASSWORD " +
            "FROM WF_BPS_PROFILE ORDER BY PROFILE_NAME, TENANT_ID LIMIT ?, ?";
    public static final String UPDATE_BPS_PASSWORD = "UPDATE WF_BPS_PROFILE SET PASSWORD=? WHERE PROFILE_NAME=? AND " +
            "TENANT_ID=?";
    public static final String GET_WF_REQUEST = "SELECT UUID, REQUEST FROM WF_REQUEST ORDER BY UUID LIMIT ?, ?";
    public static final String UPDATE_WF_REQUEST = "UPDATE WF_REQUEST SET REQUEST=? WHERE UUID=?";
}

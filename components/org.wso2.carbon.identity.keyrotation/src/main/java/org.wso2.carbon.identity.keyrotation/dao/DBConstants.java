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
 * Constant class to store DB related constants.
 */
public class DBConstants {

    public static final String DATA_KEY = "http://wso2.org/claims/identity/secretkey";
    public static final String GET_IDENTITY_SECRET = "SELECT TENANT_ID, USER_NAME, DATA_KEY, DATA_VALUE " +
            "FROM " +
            "IDN_IDENTITY_USER_DATA ORDER BY TENANT_ID, USER_NAME, DATA_KEY LIMIT ?, ?";
    public static final String UPDATE_IDENTITY_SECRET =
            "UPDATE IDN_IDENTITY_USER_DATA SET DATA_VALUE=? WHERE TENANT_ID=? AND USER_NAME=? AND DATA_KEY=?";
    public static final int CHUNK_SIZE = 2;
}

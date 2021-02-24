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
 * Class that holds constants needed for the key rotation functionality.
 */
public class KeyRotationConstants {

    public static final String ALGORITHM = "AES";
    public static final String TRANSFORMATION = "AES/GCM/NoPadding";
    public static final String PROPERTIES_FILE_PATH = System.getProperty("user.dir") +
            "/components/org.wso2.carbon.identity.keyrotation/src/main/resources/properties.yaml";
    public static final String USERSTORES = "userstores";
    public static final String SUPERTENANT_USERSTORE_PATH = "/repository/deployment/server/userstores";
    public static final String TEANANT_USERSTORE_PATH = "/repository/tenants/";
    public static final String EVENT_PUBLISHERS_PATH = "/repository/deployment/server/";
}

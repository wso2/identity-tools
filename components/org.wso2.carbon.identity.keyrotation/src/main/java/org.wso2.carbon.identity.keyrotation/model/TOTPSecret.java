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

package org.wso2.carbon.identity.keyrotation.model;

/**
 * Class to hold the TOTP secrets stored in IDN_IDENTITY_USER_DATA.
 */
public class TOTPSecret {

    private String tenantId;
    private String username;
    private String dataKey;
    private String dataValue;

    public TOTPSecret(String tenantId, String username, String dataKey, String dataValue) {

        this.tenantId = tenantId;
        this.username = username;
        this.dataKey = dataKey;
        this.dataValue = dataValue;
    }

    public String getTenantId() {

        return tenantId;
    }

    public void setTenantId(String tenantId) {

        this.tenantId = tenantId;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getDataKey() {

        return dataKey;
    }

    public void setDataKey(String dataKey) {

        this.dataKey = dataKey;
    }

    public String getDataValue() {

        return dataValue;
    }

    public void setDataValue(String dataValue) {

        this.dataValue = dataValue;
    }

}

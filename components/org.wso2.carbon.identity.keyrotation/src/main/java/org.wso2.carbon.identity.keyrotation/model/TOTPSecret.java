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
 * Class to hold the data stored in IDN_IDENTITY_USER_DATA.
 */
public class TOTPSecret {

    private String tenantId;
    private String username;
    private String dataKey;
    private String dataValue;

    /**
     * TOTPSecret class constructor.
     *
     * @param tenantId  Tenant id field in IDN_IDENTITY_USER_DATA table.
     * @param username  Username field in IDN_IDENTITY_USER_DATA table.
     * @param dataKey   Data key field in IDN_IDENTITY_USER_DATA table.
     * @param dataValue Data value field in IDN_IDENTITY_USER_DATA table.
     */
    public TOTPSecret(String tenantId, String username, String dataKey, String dataValue) {

        this.tenantId = tenantId;
        this.username = username;
        this.dataKey = dataKey;
        this.dataValue = dataValue;
    }

    /**
     * Getter to get the tenant id.
     *
     * @return Tenant id.
     */
    public String getTenantId() {

        return tenantId;
    }

    /**
     * Setter to set the tenant id.
     *
     * @param tenantId Tenant id.
     */
    public void setTenantId(String tenantId) {

        this.tenantId = tenantId;
    }

    /**
     * Getter to get the username.
     *
     * @return Username.
     */
    public String getUsername() {

        return username;
    }

    /**
     * Setter to set the username.
     *
     * @param username Username.
     */
    public void setUsername(String username) {

        this.username = username;
    }

    /**
     * Getter to get the data key.
     *
     * @return Data key.
     */
    public String getDataKey() {

        return dataKey;
    }

    /**
     * Setter to set the data key.
     *
     * @param dataKey Data key.
     */
    public void setDataKey(String dataKey) {

        this.dataKey = dataKey;
    }

    /**
     * Getter to get the data value.
     *
     * @return Data value.
     */
    public String getDataValue() {

        return dataValue;
    }

    /**
     * Setter to set the data value.
     *
     * @param dataValue Data value.
     */
    public void setDataValue(String dataValue) {

        this.dataValue = dataValue;
    }
}

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
 * This class holds the data stored in IDN_IDENTITY_USER_DATA_TEMP.
 */
public class TempTOTPSecret {

    private String tenantId;
    private String username;
    private String dataKey;
    private String dataValue;
    private String availability;

    /**
     * TempTOTPSecret class constructor.
     *
     * @param tenantId     Tenant id field in IDN_IDENTITY_USER_DATA_TEMP table.
     * @param username     Username field in IDN_IDENTITY_USER_DATA_TEMP table.
     * @param dataKey      Data key field in IDN_IDENTITY_USER_DATA_TEMP table.
     * @param dataValue    Data value field in IDN_IDENTITY_USER_DATA_TEMP table.
     * @param availability Availability field in IDN_IDENTITY_USER_DATA_TEMP table.
     */
    public TempTOTPSecret(String tenantId, String username, String dataKey, String dataValue, String availability) {

        this.tenantId = tenantId;
        this.username = username;
        this.dataKey = dataKey;
        this.dataValue = dataValue;
        this.availability = availability;
    }

    /**
     * Get for the tenant id.
     *
     * @return Tenant id.
     */
    public String getTenantId() {

        return tenantId;
    }

    /**
     * Set for the tenant id.
     *
     * @param tenantId Tenant id.
     */
    public void setTenantId(String tenantId) {

        this.tenantId = tenantId;
    }

    /**
     * Get for the username.
     *
     * @return Username.
     */
    public String getUsername() {

        return username;
    }

    /**
     * Set for the username.
     *
     * @param username Username.
     */
    public void setUsername(String username) {

        this.username = username;
    }

    /**
     * Get for the data key.
     *
     * @return Data key.
     */
    public String getDataKey() {

        return dataKey;
    }

    /**
     * Set for the data key.
     *
     * @param dataKey Data key.
     */
    public void setDataKey(String dataKey) {

        this.dataKey = dataKey;
    }

    /**
     * Get for the data value.
     *
     * @return Data value.
     */
    public String getDataValue() {

        return dataValue;
    }

    /**
     * Set for the data value.
     *
     * @param dataValue Data value.
     */
    public void setDataValue(String dataValue) {

        this.dataValue = dataValue;
    }

    /**
     * Get for the availability.
     *
     * @return Availability.
     */
    public String getAvailability() {

        return availability;
    }

    /**
     * Set for the availability.
     *
     * @param availability Availability.
     */
    public void setAvailability(String availability) {

        this.availability = availability;
    }
}

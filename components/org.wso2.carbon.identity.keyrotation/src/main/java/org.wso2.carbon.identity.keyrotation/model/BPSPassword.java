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
 * This class holds the data stored in WF_BPS_PROFILE.
 */
public class BPSPassword {

    private String profileName;
    private String tenantId;
    private String password;
    private String username;

    /**
     * BPSPassword class constructor.
     *
     * @param profileName Profile name field in WF_BPS_PROFILE table.
     * @param username    Username field in WF_BPS_PROFILE table.
     * @param tenantId    Tenant id field in WF_BPS_PROFILE table.
     * @param password    Password field in WF_BPS_PROFILE table.
     */
    public BPSPassword(String profileName, String username, String tenantId, String password) {

        this.profileName = profileName;
        this.username = username;
        this.tenantId = tenantId;
        this.password = password;
    }

    /**
     * Get for the profile name.
     *
     * @return Profile name.
     */
    public String getProfileName() {

        return profileName;
    }

    /**
     * Set for the profile name.
     *
     * @param profileName Profile name.
     */
    public void setProfileName(String profileName) {

        this.profileName = profileName;
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
     * Get for the password.
     *
     * @return Password.
     */
    public String getPassword() {

        return password;
    }

    /**
     * Set for the password.
     *
     * @param password Password.
     */
    public void setPassword(String password) {

        this.password = password;
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
}

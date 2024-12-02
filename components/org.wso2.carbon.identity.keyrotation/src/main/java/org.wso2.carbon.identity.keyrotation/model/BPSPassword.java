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

    private final String profileName;
    private final String tenantId;
    private final String password;
    private final String username;
    private String newPassword;

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
     * Get for the tenant id.
     *
     * @return Tenant id.
     */
    public String getTenantId() {

        return tenantId;
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
     * Get for the username.
     *
     * @return Username.
     */
    public String getUsername() {

        return username;
    }

    public String getNewPassword() {

        return newPassword;
    }

    public void setNewPassword(String newPassword) {

        this.newPassword = newPassword;
    }
}

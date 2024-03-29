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
 * This class holds the data stored in IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP.
 */
public class TempOAuthScope {

    private String tokenId;
    private String tokenScope;
    private String tenantId;
    private int availability;
    private int syncId;
    private int synced;

    /**
     * TempOAuthScope class constructor.
     *
     * @param tokenId      Token id field in IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP table.
     * @param tokenScope   Token scope field in IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP table.
     * @param tenantId     Tenant id field in IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP table.
     * @param availability Availability field in IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP table.
     * @param syncId       Sync id field in IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP table.
     * @param synced       Synced field in IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP table.
     */
    public TempOAuthScope(String tokenId, String tokenScope, String tenantId, int availability, int syncId,
                          int synced) {

        this.tokenId = tokenId;
        this.tokenScope = tokenScope;
        this.tenantId = tenantId;
        this.availability = availability;
        this.syncId = syncId;
        this.synced = synced;
    }

    /**
     * Get for the token id.
     *
     * @return Token id.
     */
    public String getTokenId() {

        return tokenId;
    }

    /**
     * Set for the token id.
     *
     * @param tokenId Token id.
     */
    public void setTokenId(String tokenId) {

        this.tokenId = tokenId;
    }

    /**
     * Get for the token scope.
     *
     * @return Token scope.
     */
    public String getTokenScope() {

        return tokenScope;
    }

    /**
     * Set for the token scope.
     *
     * @param tokenScope Token scope.
     */
    public void setTokenScope(String tokenScope) {

        this.tokenScope = tokenScope;
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
     * Get for the availability.
     *
     * @return Availability.
     */
    public int getAvailability() {

        return availability;
    }

    /**
     * Set for the availability.
     *
     * @param availability Availability.
     */
    public void setAvailability(int availability) {

        this.availability = availability;
    }

    /**
     * Get for the sync id.
     *
     * @return Sync id.
     */
    public int getSyncId() {

        return syncId;
    }

    /**
     * Set for the sync id.
     *
     * @param syncId Sync id.
     */
    public void setSyncId(int syncId) {

        this.syncId = syncId;
    }

    /**
     * Get for the synced.
     *
     * @return Synced.
     */
    public int getSynced() {

        return synced;
    }

    /**
     * Set for the synced.
     *
     * @param synced Synced.
     */
    public void setSynced(int synced) {

        this.synced = synced;
    }
}

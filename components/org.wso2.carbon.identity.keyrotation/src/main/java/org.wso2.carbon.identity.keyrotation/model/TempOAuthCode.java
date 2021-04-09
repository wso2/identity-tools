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
 * This class holds the data stored in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP.
 */
public class TempOAuthCode {

    private String codeId;
    private String authorizationCode;
    private String consumerKeyId;
    private String callbackUrl;
    private String scope;
    private String authzUser;
    private String tenantId;
    private String userDomain;
    private String timeCreated;
    private String validityPeriod;
    private String state;
    private String tokenId;
    private String subjectIdentifier;
    private String pkceCodeChallenge;
    private String pkceCodeChallengeMethod;
    private String authorizationCodeHash;
    private String idpId;
    private int availability;
    private int syncId;
    private int synced;

    /**
     * TempOAuthCode class constructor.
     *
     * @param codeId                  Code id field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param authorizationCode       Authorization code field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param consumerKeyId           Consumer key id field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param callbackUrl             Callback url field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param scope                   Scope field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param authzUser               Authz user field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param tenantId                Tenant id field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param userDomain              User domain field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param timeCreated             Time created field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param validityPeriod          Validity period field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param state                   State field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param tokenId                 Token id field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param subjectIdentifier       Subject identifier field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param pkceCodeChallenge       Pkce code challenge field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param pkceCodeChallengeMethod Pkce code challenge method field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param authorizationCodeHash   Authorization code hash field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param idpId                   Idp id field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param availability            Availability field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param syncId                  Sync id field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param synced                  Synced field in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     */
    public TempOAuthCode(String codeId, String authorizationCode, String consumerKeyId, String callbackUrl,
                         String scope, String authzUser, String tenantId, String userDomain, String timeCreated,
                         String validityPeriod,
                         String state, String tokenId, String subjectIdentifier, String pkceCodeChallenge,
                         String pkceCodeChallengeMethod, String authorizationCodeHash, String idpId,
                         int availability, int syncId, int synced) {

        this.codeId = codeId;
        this.authorizationCode = authorizationCode;
        this.consumerKeyId = consumerKeyId;
        this.callbackUrl = callbackUrl;
        this.scope = scope;
        this.authzUser = authzUser;
        this.tenantId = tenantId;
        this.userDomain = userDomain;
        this.timeCreated = timeCreated;
        this.validityPeriod = validityPeriod;
        this.state = state;
        this.tokenId = tokenId;
        this.subjectIdentifier = subjectIdentifier;
        this.pkceCodeChallenge = pkceCodeChallenge;
        this.pkceCodeChallengeMethod = pkceCodeChallengeMethod;
        this.authorizationCodeHash = authorizationCodeHash;
        this.idpId = idpId;
        this.availability = availability;
        this.syncId = syncId;
        this.synced = synced;

    }

    /**
     * Get for the code id.
     *
     * @return Code id.
     */
    public String getCodeId() {

        return codeId;
    }

    /**
     * Set for the code id.
     *
     * @param codeId Code id.
     */
    public void setCodeId(String codeId) {

        this.codeId = codeId;
    }

    /**
     * Get for the authorization code.
     *
     * @return Authorization code.
     */
    public String getAuthorizationCode() {

        return authorizationCode;
    }

    /**
     * Set for the authorization code.
     *
     * @param authorizationCode Authorization code.
     */
    public void setAuthorizationCode(String authorizationCode) {

        this.authorizationCode = authorizationCode;
    }

    /**
     * Get for the consumer key id.
     *
     * @return Consumer key id.
     */
    public String getConsumerKeyId() {

        return consumerKeyId;
    }

    /**
     * Set for the consumer key id.
     *
     * @param consumerKeyId Consumer key id.
     */
    public void setConsumerKeyId(String consumerKeyId) {

        this.consumerKeyId = consumerKeyId;
    }

    /**
     * Get for the callback url.
     *
     * @return Callback url.
     */
    public String getCallbackUrl() {

        return callbackUrl;
    }

    /**
     * Set for the callback url.
     *
     * @param callbackUrl Callback url.
     */
    public void setCallbackUrl(String callbackUrl) {

        this.callbackUrl = callbackUrl;
    }

    /**
     * Get for the scope.
     *
     * @return Scope.
     */
    public String getScope() {

        return scope;
    }

    /**
     * Set for the scope.
     *
     * @param scope Scope.
     */
    public void setScope(String scope) {

        this.scope = scope;
    }

    /**
     * Get for the authz user.
     *
     * @return Authz user.
     */
    public String getAuthzUser() {

        return authzUser;
    }

    /**
     * Set for the authz user.
     *
     * @param authzUser Authz user.
     */
    public void setAuthzUser(String authzUser) {

        this.authzUser = authzUser;
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
     * Get for the user domain.
     *
     * @return User domain.
     */
    public String getUserDomain() {

        return userDomain;
    }

    /**
     * Set for the user domain.
     *
     * @param userDomain User domain.
     */
    public void setUserDomain(String userDomain) {

        this.userDomain = userDomain;
    }

    /**
     * Get for the time created.
     *
     * @return Time created.
     */
    public String getTimeCreated() {

        return timeCreated;
    }

    /**
     * Set for the time created.
     *
     * @param timeCreated Time created.
     */
    public void setTimeCreated(String timeCreated) {

        this.timeCreated = timeCreated;
    }

    /**
     * Get for the validity period.
     *
     * @return Validity period.
     */
    public String getValidityPeriod() {

        return validityPeriod;
    }

    /**
     * Set for the validity period.
     *
     * @param validityPeriod Validity period.
     */
    public void setValidityPeriod(String validityPeriod) {

        this.validityPeriod = validityPeriod;
    }

    /**
     * Get for the state.
     *
     * @return State.
     */
    public String getState() {

        return state;
    }

    /**
     * Set for the state.
     *
     * @param state State.
     */
    public void setState(String state) {

        this.state = state;
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
     * Get for the subject identifier.
     *
     * @return Subject identifier.
     */
    public String getSubjectIdentifier() {

        return subjectIdentifier;
    }

    /**
     * Set for the subject identifier.
     *
     * @param subjectIdentifier Subject identifier.
     */
    public void setSubjectIdentifier(String subjectIdentifier) {

        this.subjectIdentifier = subjectIdentifier;
    }

    /**
     * Get for the pkce code challenge.
     *
     * @return Pkce code challenge.
     */
    public String getPkceCodeChallenge() {

        return pkceCodeChallenge;
    }

    /**
     * Set for the pkce code challenge.
     *
     * @param pkceCodeChallenge Pkce code challenge.
     */
    public void setPkceCodeChallenge(String pkceCodeChallenge) {

        this.pkceCodeChallenge = pkceCodeChallenge;
    }

    /**
     * Get for the pkce code challenge method.
     *
     * @return Pkce code challenge method.
     */
    public String getPkceCodeChallengeMethod() {

        return pkceCodeChallengeMethod;
    }

    /**
     * Set for the pkce code challenge method.
     *
     * @param pkceCodeChallengeMethod Pkce code challenge method.
     */
    public void setPkceCodeChallengeMethod(String pkceCodeChallengeMethod) {

        this.pkceCodeChallengeMethod = pkceCodeChallengeMethod;
    }

    /**
     * Get for the authorization code hash.
     *
     * @return Authorization code hash.
     */
    public String getAuthorizationCodeHash() {

        return authorizationCodeHash;
    }

    /**
     * Set for the authorization code hash.
     *
     * @param authorizationCodeHash Authorization code hash.
     */
    public void setAuthorizationCodeHash(String authorizationCodeHash) {

        this.authorizationCodeHash = authorizationCodeHash;
    }

    /**
     * Get for the idp id.
     *
     * @return Idp id.
     */
    public String getIdpId() {

        return idpId;
    }

    /**
     * Set for the idp id.
     *
     * @param idpId Idp id.
     */
    public void setIdpId(String idpId) {

        this.idpId = idpId;
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

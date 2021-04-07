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
 * This class holds the data stored in IDN_OAUTH2_ACCESS_TOKEN_TEMP.
 */
public class TempOAuthToken {

    private String tokenId;
    private String accessToken;
    private String refreshToken;
    private String consumerKeyId;
    private String authzUser;
    private String tenantId;
    private String userDomain;
    private String userType;
    private String grantType;
    private String timeCreated;
    private String refreshTokenTimeCreated;
    private String validityPeriod;
    private String refreshTokenValidityPeriod;
    private String tokenScopeHash;
    private String tokenState;
    private String tokenStateId;
    private String subjectIdentifier;
    private String accessTokenHash;
    private String refreshTokenHash;
    private String idpId;
    private String tokenBindingRef;
    private String availability;

    /**
     * TempOAuthToken class constructor.
     *
     * @param tokenId                    Token id field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param accessToken                Access token field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param refreshToken               Refresh token field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param consumerKeyId              Consumer key id field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param authzUser                  Authz user field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param tenantId                   Tenant id field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param userDomain                 User domain field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param userType                   User type field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param grantType                  Grant type field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param timeCreated                Time created field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param refreshTokenTimeCreated    Refresh token Time Created field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param validityPeriod             Validity period field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param refreshTokenValidityPeriod Refresh token validity period field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param tokenScopeHash             Token scope hash field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param tokenState                 Token state field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param tokenStateId               Token state id field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param subjectIdentifier          Subject identifier field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param accessTokenHash            Access token hash field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param refreshTokenHash           Refresh token hash field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param idpId                      IdpId field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param tokenBindingRef            Token binding ref field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param availability               Availability field in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     */
    public TempOAuthToken(String tokenId, String accessToken, String refreshToken, String consumerKeyId,
                          String authzUser, String tenantId, String userDomain, String userType, String grantType,
                          String timeCreated, String refreshTokenTimeCreated, String validityPeriod,
                          String refreshTokenValidityPeriod, String tokenScopeHash, String tokenState,
                          String tokenStateId, String subjectIdentifier, String accessTokenHash,
                          String refreshTokenHash, String idpId, String tokenBindingRef, String availability) {

        this.tokenId = tokenId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.consumerKeyId = consumerKeyId;
        this.authzUser = authzUser;
        this.tenantId = tenantId;
        this.userDomain = userDomain;
        this.userType = userType;
        this.grantType = grantType;
        this.timeCreated = timeCreated;
        this.refreshTokenTimeCreated = refreshTokenTimeCreated;
        this.validityPeriod = validityPeriod;
        this.refreshTokenValidityPeriod = refreshTokenValidityPeriod;
        this.tokenScopeHash = tokenScopeHash;
        this.tokenState = tokenState;
        this.tokenStateId = tokenStateId;
        this.subjectIdentifier = subjectIdentifier;
        this.accessTokenHash = accessTokenHash;
        this.refreshTokenHash = refreshTokenHash;
        this.idpId = idpId;
        this.tokenBindingRef = tokenBindingRef;
        this.availability = availability;
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
     * Get for the access token.
     *
     * @return Access token.
     */
    public String getAccessToken() {

        return accessToken;
    }

    /**
     * Set for the access token.
     *
     * @param accessToken Access token.
     */
    public void setAccessToken(String accessToken) {

        this.accessToken = accessToken;
    }

    /**
     * Get for the refresh token.
     *
     * @return Refresh token.
     */
    public String getRefreshToken() {

        return refreshToken;
    }

    /**
     * Set for the refresh token.
     *
     * @param refreshToken Refresh token.
     */
    public void setRefreshToken(String refreshToken) {

        this.refreshToken = refreshToken;
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
     * Get for the user type.
     *
     * @return User type.
     */
    public String getUserType() {

        return userType;
    }

    /**
     * Set for the user type.
     *
     * @param userType User type.
     */
    public void setUserType(String userType) {

        this.userType = userType;
    }

    /**
     * Get for the grant type.
     *
     * @return Grant type.
     */
    public String getGrantType() {

        return grantType;
    }

    /**
     * Set for the grant type.
     *
     * @param grantType Grant type.
     */
    public void setGrantType(String grantType) {

        this.grantType = grantType;
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
     * Get for the refresh token time created.
     *
     * @return Refresh token time created.
     */
    public String getRefreshTokenTimeCreated() {

        return refreshTokenTimeCreated;
    }

    /**
     * Set for the refresh token time created.
     *
     * @param refreshTokenTimeCreated Refresh token time created.
     */
    public void setRefreshTokenTimeCreated(String refreshTokenTimeCreated) {

        this.refreshTokenTimeCreated = refreshTokenTimeCreated;
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
     * Get for the refresh token validity period.
     *
     * @return Refresh token validity period.
     */
    public String getRefreshTokenValidityPeriod() {

        return refreshTokenValidityPeriod;
    }

    /**
     * Set for the refresh token validity period.
     *
     * @param refreshTokenValidityPeriod Refresh token validity period.
     */
    public void setRefreshTokenValidityPeriod(String refreshTokenValidityPeriod) {

        this.refreshTokenValidityPeriod = refreshTokenValidityPeriod;
    }

    /**
     * Get for the token scope hash.
     *
     * @return Token scope hash.
     */
    public String getTokenScopeHash() {

        return tokenScopeHash;
    }

    /**
     * Set for the token scope hash.
     *
     * @param tokenScopeHash Token scope hash.
     */
    public void setTokenScopeHash(String tokenScopeHash) {

        this.tokenScopeHash = tokenScopeHash;
    }

    /**
     * Get for the token state.
     *
     * @return Token state.
     */
    public String getTokenState() {

        return tokenState;
    }

    /**
     * Set for the token state.
     *
     * @param tokenState Token state.
     */
    public void setTokenState(String tokenState) {

        this.tokenState = tokenState;
    }

    /**
     * Get for the token state id.
     *
     * @return Token state id.
     */
    public String getTokenStateId() {

        return tokenStateId;
    }

    /**
     * Set for the token state id.
     *
     * @param tokenStateId Token state id.
     */
    public void setTokenStateId(String tokenStateId) {

        this.tokenStateId = tokenStateId;
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
     * Get for the access token hash.
     *
     * @return Access token hash.
     */
    public String getAccessTokenHash() {

        return accessTokenHash;
    }

    /**
     * Set for the access token hash.
     *
     * @param accessTokenHash Access token hash.
     */
    public void setAccessTokenHash(String accessTokenHash) {

        this.accessTokenHash = accessTokenHash;
    }

    /**
     * Get for the refresh token hash.
     *
     * @return Refresh token hash.
     */
    public String getRefreshTokenHash() {

        return refreshTokenHash;
    }

    /**
     * Set for the refresh token hash.
     *
     * @param refreshTokenHash Refresh token hash.
     */
    public void setRefreshTokenHash(String refreshTokenHash) {

        this.refreshTokenHash = refreshTokenHash;
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
     * Get for the token binding ref.
     *
     * @return Token binding ref.
     */
    public String getTokenBindingRef() {

        return tokenBindingRef;
    }

    /**
     * Set for the token binding Ref.
     *
     * @param tokenBindingRef Token binding Ref.
     */
    public void setTokenBindingRef(String tokenBindingRef) {

        this.tokenBindingRef = tokenBindingRef;
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

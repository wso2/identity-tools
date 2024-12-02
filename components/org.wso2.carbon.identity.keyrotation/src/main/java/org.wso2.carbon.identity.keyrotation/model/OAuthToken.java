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
 * This class holds the data stored in IDN_OAUTH2_ACCESS_TOKEN.
 */
public class OAuthToken {

    private final String tokenId;
    private final String accessToken;
    private final String refreshToken;
    private final String consumerKeyId;
    private String newAccessToken;
    private String newRefreshToken;

    /**
     * OAuthToken class constructor.
     *
     * @param tokenId       Token id field in IDN_OAUTH2_ACCESS_TOKEN table.
     * @param accessToken   Access token field in IDN_OAUTH2_ACCESS_TOKEN table.
     * @param refreshToken  Refresh token field in IDN_OAUTH2_ACCESS_TOKEN table.
     * @param consumerKeyId Consumer key id field in IDN_OAUTH2_ACCESS_TOKEN table.
     */
    public OAuthToken(String tokenId, String accessToken, String refreshToken, String consumerKeyId) {

        this.tokenId = tokenId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.consumerKeyId = consumerKeyId;
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
     * Get for the access token.
     *
     * @return Access token.
     */
    public String getAccessToken() {

        return accessToken;
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
     * Get for the consumer key id.
     *
     * @return Consumer key id.
     */
    public String getConsumerKeyId() {

        return consumerKeyId;
    }

    public String getNewAccessToken() {

        return newAccessToken;
    }

    public void setNewAccessToken(String newAccessToken) {

        this.newAccessToken = newAccessToken;
    }

    public String getNewRefreshToken() {

        return newRefreshToken;
    }

    public void setNewRefreshToken(String newRefreshToken) {

        this.newRefreshToken = newRefreshToken;
    }
}

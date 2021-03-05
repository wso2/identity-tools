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
 * Class to hold the data stored in IDN_OAUTH2_ACCESS_TOKEN.
 */
public class OAuthToken {

    private String tokenId;
    private String accessToken;
    private String consumerKeyId;

    public OAuthToken(String tokenId, String accessToken, String consumerKeyId) {

        this.tokenId = tokenId;
        this.accessToken = accessToken;
        this.consumerKeyId = consumerKeyId;
    }

    public String getTokenId() {

        return tokenId;
    }

    public void setTokenId(String tokenId) {

        this.tokenId = tokenId;
    }

    public String getAccessToken() {

        return accessToken;
    }

    public void setAccessToken(String accessToken) {

        this.accessToken = accessToken;
    }

    public String getConsumerKeyId() {

        return consumerKeyId;
    }

    public void setConsumerKeyId(String consumerKeyId) {

        this.consumerKeyId = consumerKeyId;
    }
}

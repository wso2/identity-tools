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
 * Class to hold the data stored in IDN_OAUTH2_AUTHORIZATION_CODE.
 */
public class OAuthCode {

    private String codeId;
    private String authorizationCode;
    private String consumerKeyId;

    /**
     * OAuthCode class constructor.
     *
     * @param codeId            Code id field in IDN_OAUTH2_AUTHORIZATION_CODE table.
     * @param authorizationCode Authorization code field in IDN_OAUTH2_AUTHORIZATION_CODE table.
     * @param consumerKeyId     Consumer key id field in IDN_OAUTH2_AUTHORIZATION_CODE table.
     */
    public OAuthCode(String codeId, String authorizationCode, String consumerKeyId) {

        this.codeId = codeId;
        this.authorizationCode = authorizationCode;
        this.consumerKeyId = consumerKeyId;
    }

    /**
     * Getter to get the code id.
     *
     * @return Code id.
     */
    public String getCodeId() {

        return codeId;
    }

    /**
     * Setter to set the code id.
     *
     * @param codeId Code id.
     */
    public void setCodeId(String codeId) {

        this.codeId = codeId;
    }

    /**
     * Getter to get the authorization code.
     *
     * @return Authorization code.
     */
    public String getAuthorizationCode() {

        return authorizationCode;
    }

    /**
     * Setter to set the authorization code.
     *
     * @param authorizationCode Authorization code.
     */
    public void setAuthorizationCode(String authorizationCode) {

        this.authorizationCode = authorizationCode;
    }

    /**
     * Getter to get the consumer key id.
     *
     * @return Consumer key id.
     */
    public String getConsumerKeyId() {

        return consumerKeyId;
    }

    /**
     * Setter to set the consumer key id.
     *
     * @param consumerKeyId Consumer key id.
     */
    public void setConsumerKeyId(String consumerKeyId) {

        this.consumerKeyId = consumerKeyId;
    }
}

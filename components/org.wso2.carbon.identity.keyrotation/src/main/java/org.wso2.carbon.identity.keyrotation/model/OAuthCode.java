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

    public OAuthCode(String codeId, String authorizationCode, String consumerKeyId) {

        this.codeId = codeId;
        this.authorizationCode = authorizationCode;
        this.consumerKeyId = consumerKeyId;
    }

    public String getCodeId() {

        return codeId;
    }

    public void setCodeId(String codeId) {

        this.codeId = codeId;
    }

    public String getAuthorizationCode() {

        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {

        this.authorizationCode = authorizationCode;
    }

    public String getConsumerKeyId() {

        return consumerKeyId;
    }

    public void setConsumerKeyId(String consumerKeyId) {

        this.consumerKeyId = consumerKeyId;
    }
}

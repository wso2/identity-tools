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
 * Class to hold the consumer secrets in IDN_OAUTH_CONSUMER_APPS.
 */
public class OAuthSecret {

    private String id;
    private String consumerSecret;
    private String appName;

    public OAuthSecret(String id, String consumerSecret, String appName) {

        this.id = id;
        this.consumerSecret = consumerSecret;
        this.appName = appName;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getConsumerSecret() {

        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {

        this.consumerSecret = consumerSecret;
    }

    public String getAppName() {

        return appName;
    }

    public void setAppName(String appName) {

        this.appName = appName;
    }
}

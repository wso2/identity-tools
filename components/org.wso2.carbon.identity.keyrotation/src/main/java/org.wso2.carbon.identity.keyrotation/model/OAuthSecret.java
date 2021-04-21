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
 * This class holds the data stored in IDN_OAUTH_CONSUMER_APPS.
 */
public class OAuthSecret {

    private String id;
    private String consumerSecret;
    private String appName;

    /**
     * OAuthSecret class constructor.
     *
     * @param id             Id field in IDN_OAUTH_CONSUMER_APPS table.
     * @param consumerSecret Consumer secret field in IDN_OAUTH_CONSUMER_APPS table.
     * @param appName        App name field in IDN_OAUTH_CONSUMER_APPS table.
     */
    public OAuthSecret(String id, String consumerSecret, String appName) {

        this.id = id;
        this.consumerSecret = consumerSecret;
        this.appName = appName;
    }

    /**
     * Get for the id.
     *
     * @return Id.
     */
    public String getId() {

        return id;
    }

    /**
     * Set for the id.
     *
     * @param id Id.
     */
    public void setId(String id) {

        this.id = id;
    }

    /**
     * Get for the consumer secret.
     *
     * @return Consumer secret.
     */
    public String getConsumerSecret() {

        return consumerSecret;
    }

    /**
     * Set for the consumer secret.
     *
     * @param consumerSecret Consumer secret.
     */
    public void setConsumerSecret(String consumerSecret) {

        this.consumerSecret = consumerSecret;
    }

    /**
     * Get for the app name.
     *
     * @return App name.
     */
    public String getAppName() {

        return appName;
    }

    /**
     * Set for the app name.
     *
     * @param appName App name.
     */
    public void setAppName(String appName) {

        this.appName = appName;
    }
}

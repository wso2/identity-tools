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
package org.wso2.carbon.identity.keyrotation.util;

/**
 * Class to map the properties.yaml file content.
 */
public class MigrationConfig {

    private String oldDatabase;
    private String newDatabase;
    private String eventPublisherCore;
    private String secondaryUserstore;
    private String tenantUserStore;

    public MigrationConfig(String oldDatabase, String newDatabase, String eventPublisherCore,
                           String secondaryUserstore, String tenantUserStore) {

        this.oldDatabase = oldDatabase;
        this.newDatabase = newDatabase;
        this.eventPublisherCore = eventPublisherCore;
        this.secondaryUserstore = secondaryUserstore;
        this.tenantUserStore = tenantUserStore;
    }

    public MigrationConfig() {

    }

    public String getOldDatabase() {

        return oldDatabase;
    }

    public void setOldDatabase(String oldDatabase) {

        this.oldDatabase = oldDatabase;
    }

    public String getNewDatabase() {

        return newDatabase;
    }

    public void setNewDatabase(String newDatabase) {

        this.newDatabase = newDatabase;
    }

    public String getEventPublisherCore() {

        return eventPublisherCore;
    }

    public void setEventPublisherCore(String eventPublisherCore) {

        this.eventPublisherCore = eventPublisherCore;
    }

    public String getSecondaryUserstore() {

        return secondaryUserstore;
    }

    public void setSecondaryUserstore(String secondaryUserstore) {

        this.secondaryUserstore = secondaryUserstore;
    }

    public String getTenantUserStore() {

        return tenantUserStore;
    }

    public void setTenantUserStore(String tenantUserStore) {

        this.tenantUserStore = tenantUserStore;
    }

    @Override
    public String toString() {

        return "\noldDatabase: " + oldDatabase + "\nnewDatabase: " + newDatabase + "\neventPublisherCore: " +
                eventPublisherCore + "\nsecondaryUserstore: " + secondaryUserstore + "\ntenantUserStore: " +
                tenantUserStore + "\n";
    }
}

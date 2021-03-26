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
 * Class to hold the registry properties stored in REG_PROPERTY.
 */
public class RegistryProperty {

    private String regId;
    private String regName;
    private String regValue;
    private String regTenantId;

    /**
     * RegistryProperty class constructor.
     *
     * @param regId       Registry id field in REG_PROPERTY table.
     * @param regName     Registry name field in REG_PROPERTY table.
     * @param regValue    Registry value field in REG_PROPERTY table.
     * @param regTenantId Registry tenant id field in REG_PROPERTY table.
     */
    public RegistryProperty(String regId, String regName, String regValue, String regTenantId) {

        this.regId = regId;
        this.regName = regName;
        this.regValue = regValue;
        this.regTenantId = regTenantId;
    }

    /**
     * Getter to get the registry id.
     *
     * @return Registry id.
     */
    public String getRegId() {

        return regId;
    }

    /**
     * Setter to set the registry id.
     *
     * @param regId Registry id.
     */
    public void setRegId(String regId) {

        this.regId = regId;
    }

    /**
     * Getter to get the registry name.
     *
     * @return Registry name.
     */
    public String getRegName() {

        return regName;
    }

    /**
     * Setter to set the registry name.
     *
     * @param regName Registry name.
     */
    public void setRegName(String regName) {

        this.regName = regName;
    }

    /**
     * Getter to get the registry value.
     *
     * @return Registry value.
     */
    public String getRegValue() {

        return regValue;
    }

    /**
     * Setter to set the registry value.
     *
     * @param regValue Registry value.
     */
    public void setRegValue(String regValue) {

        this.regValue = regValue;
    }

    /**
     * Getter to get the registry tenant id.
     *
     * @return Registry tenant id.
     */
    public String getRegTenantId() {

        return regTenantId;
    }

    /**
     * Setter to set the registry tenant id.
     *
     * @param regTenantId Registry tenant id.
     */
    public void setRegTenantId(String regTenantId) {

        this.regTenantId = regTenantId;
    }
}

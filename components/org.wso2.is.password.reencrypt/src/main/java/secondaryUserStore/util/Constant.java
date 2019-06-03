/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package secondaryUserStore.util;

/**
 * Holds common constants in migration service.
 */
public class Constant {

    public static final String JVM_PROPERTY_MIGRATE_PASSWORD = "reEncryptSecondaryUserStorePassword";
    public static final String CARBON_HOME = "carbon.home";
    public static final int SUPER_TENANT_ID = -1234;
    public static final String MIGRATION_LOG = " WSO2 Product Migration Service Task : ";
    public static final String CIPHER_TRANSFORMATION_SYSTEM_PROPERTY = "org.wso2.CipherTransformation";
    public static final char[] HEX_CHARACTERS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
            'C', 'D', 'E', 'F'};

}

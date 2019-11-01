/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.config.diff.creater.utils;

/**
 * Constant class used in config migration.
 */
public class MigrationConstants {

    public static final String CSV_SEPARATOR_APPENDER = "|";
    public static final String CONF_PATH = "/repository/conf";
    public static final String TEMPLATE_CONF_PATH = "/repository/resources/conf/templates/repository/conf";
    public static final String FILE_SEPARATOR = "/";
    public static final String CATALOG = "catalog";
    public static final String KEY_VALUES = "keyValues";
    public static final String OUTPUT_FOLDER = "output";
    public static final String UN_TEMPLATE_FILE_FOLDER = "unTemplatedFiles";
    public static final String NEW_LINE = "\n";
    public static final String CATALOG_URL = "https://raw.githubusercontent.com/wso2/identity-tools/components/" +
            "resources/Catalog.csv";
    public static final String OUTPUT_CATALOG_CSV = "outputCatalog.csv";
    public static final String KEY_VALUE_CSV = "keyValue.csv";

    public static final String XML_FILE_EXTENSION = ".xml";
    public static final String PROPERTY_FILE_EXTENSION = ".properties";
    public static final String J2_FILE_EXTENSION = ".j2";

}
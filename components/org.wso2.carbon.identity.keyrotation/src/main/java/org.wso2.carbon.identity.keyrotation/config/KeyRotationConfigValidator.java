/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.keyrotation.config;

import org.apache.commons.lang3.BooleanUtils;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Validator for configuration properties retrieved by the {@link KeyRotationConfigProvider}.
 */
public class KeyRotationConfigValidator {

    private static final KeyRotationConfigValidator instance = new KeyRotationConfigValidator();

    public static KeyRotationConfigValidator getInstance() {

        return instance;
    }

    /**
     * Validate URI syntax.
     *
     * @param name Parameter name.
     * @param value URI value.
     */
    public void validateURI(String name, String value) throws KeyRotationException {

        try {
            new URI(value);
        } catch (URISyntaxException e) {
            String message = String.format("invalid URI: %s for %s", value, name);
            throw new KeyRotationException(message, e);
        }
    }

    /**
     * Validate boolean.
     *
     * @param name Parameter name.
     * @param value Boolean value.
     */
    public void validateBoolean(String name, String value) throws KeyRotationException {

        Boolean bool = BooleanUtils.toBooleanObject(value);
        if (bool == null) {
            String message = String.format("invalid boolean value: %s for %s", value, name);
            throw new KeyRotationException(message);
        }
    }

    /**
     * Validate if the input path is a file or a directory..
     *
     * @param pathName Parameter name.
     * @param pathValue Boolean value.
     */
    public void validateFilePath(String pathName, String pathValue) throws KeyRotationException {

        File file = new File(pathValue);
        if (!file.exists()) {
            String message = String.format("file/directory path: %s for %s does not exist", pathValue, pathName);
            throw new KeyRotationException(message);
        }
    }

}

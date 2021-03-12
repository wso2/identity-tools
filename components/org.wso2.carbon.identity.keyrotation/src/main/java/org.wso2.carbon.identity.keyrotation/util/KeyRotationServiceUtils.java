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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.axiom.om.util.Base64;
import org.wso2.carbon.identity.keyrotation.config.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.model.CipherInitializationVector;
import org.wso2.carbon.identity.keyrotation.model.CipherMetaData;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class to store the key-rotation utility methods.
 */
public class KeyRotationServiceUtils {

    /**
     * Creates and returns a self contained cipherText with IV.
     *
     * @param cipherText The ciphertext.
     * @param iv         The Initialization Vector.
     * @return The Base64 encoded cipherWithMetaDataStr as a byte array.
     */
    public static String getSelfContainedCiphertextWithIv(byte[] cipherText, byte[] iv) {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        CipherInitializationVector cipherInitializationVector = new CipherInitializationVector();
        cipherInitializationVector.setCipher(Base64.encode(cipherText));
        cipherInitializationVector.setInitializationVector(Base64.encode(iv));
        String cipherWithMetadataStr = gson.toJson(cipherInitializationVector);
        return Base64.encode(cipherWithMetadataStr.getBytes(Charset.defaultCharset()));
    }

    /**
     * Set ciphertext and IV within the CipherMetaData object.
     *
     * @param cipherText The ciphertext.
     * @return The CipherMetaData object containing the ciphertext  and IV.
     */
    public static CipherMetaData setIvAndOriginalCipherText(byte[] cipherText) {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String cipherStr = new String(cipherText, Charset.defaultCharset());
        CipherInitializationVector cipherInitializationVector = gson.fromJson(cipherStr,
                CipherInitializationVector.class);
        CipherMetaData cipherMetaData = new CipherMetaData();
        cipherMetaData.setIv(cipherInitializationVector.getInitializationVector());
        cipherMetaData.setCipherText(cipherInitializationVector.getCipher());
        return cipherMetaData;
    }

    /**
     * To load the configurations of the properties.yaml file inside the resources directory.
     *
     * @param configFilePath Properties.yaml file absolute path.
     * @return An object of the KeyRotationConfig class.
     */
    public static KeyRotationConfig loadKeyRotationConfig(String configFilePath) throws KeyRotationException {

        Path path = Paths.get(configFilePath);
        if (Files.exists(path)) {
            try {
                Reader reader = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8);
                Yaml yaml = new Yaml();
                return yaml.loadAs(reader, KeyRotationConfig.class);
            } catch (IOException e) {
                throw new KeyRotationException("Error occurred while loading the yaml file: ", e);
            }
        } else {
            throw new KeyRotationException("File does not exist at: " + configFilePath);
        }
    }
}
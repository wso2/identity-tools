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
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.keyrotation.config.model.KeyRotationConfig;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class holds the key rotation utility methods.
 */
public class KeyRotationServiceUtils {

    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private static final String DIGEST_ALGORITHM_SHA256 = "SHA-256";

    /**
     * Creates and returns a self-contained cipherText with IV.
     *
     * @param cipherText The ciphertext.
     * @param iv         The Initialization Vector.
     * @param kid        key_id of the secret key.
     * @return The Base64 encoded cipherWithMetaDataStr.
     */
    public static String getSelfContainedCiphertextWithIv(byte[] cipherText, byte[] iv, String kid) {

        CipherInitializationVector cipherInitializationVector = new CipherInitializationVector();
        cipherInitializationVector.setCipher(Base64.encode(cipherText));

        if (StringUtils.isNotBlank(kid)) {
            cipherInitializationVector.setKeyId(kid);
        }

        cipherInitializationVector.setInitializationVector(Base64.encode(iv));
        String cipherWithMetadataStr = gson.toJson(cipherInitializationVector);

        return Base64.encode(cipherWithMetadataStr.getBytes(Charset.defaultCharset()));
    }

    /**
     * Set ciphertext and IV within the CipherMetaData object.
     *
     * @param cipherText The ciphertext.
     * @return The CipherMetaData object containing the ciphertext, IV and transformation.
     */
    public static CipherMetaData setIvAndOriginalCipherText(byte[] cipherText) {

        String cipherStr = new String(cipherText, Charset.defaultCharset());
        CipherInitializationVector cipherInitializationVector =
                gson.fromJson(cipherStr, CipherInitializationVector.class);
        CipherMetaData cipherMetaData = new CipherMetaData();
        cipherMetaData.setIv(cipherInitializationVector.getInitializationVector());
        cipherMetaData.setCipherText(cipherInitializationVector.getCipher());
        cipherMetaData.setKeyId(cipherInitializationVector.getKeyId());
        return cipherMetaData;
    }

    /**
     * To load the configurations of the properties.yaml file of the provided path.
     *
     * @param configFilePath Properties.yaml file path.
     * @return An object of the KeyRotationConfig class.
     * @throws KeyRotationException Exception thrown while loading the properties.yaml file.
     */
    public static KeyRotationConfig loadKeyRotationConfig(String configFilePath) throws KeyRotationException {

        Path path = Paths.get(configFilePath);
        try {
            Reader reader = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8);
            Yaml yaml = new Yaml();
            return yaml.loadAs(reader, KeyRotationConfig.class);
        } catch (IOException e) {
            throw new KeyRotationException("Error occurred while loading the yaml file: ", e);
        }
    }

    /**
     * Determines the encoding type based on the length of the provided secret and encodes it accordingly.
     * If the secret is 64 characters (AES-256) or 48 characters (AES-192), it is assumed to be in hex format,
     * and the method will decode it from hex to a byte array. Otherwise, the secret is encoded using the default
     * byte encoding (UTF-8).
     *
     * @param secret The secret string to be encoded.
     * @return A byte array representing the encoded secret.
     * @throws SecurityException If the secret string is in hex format but contains invalid characters or is improperly
     *                           formatted.
     */
    private static byte[] determineEncodingAndEncode(String secret) {

        // Use hex encoding if the secret is AES-256 (64 characters) or AES-192 (48 characters).
        if (secret.length() == 64 || secret.length() == 48) {
            try {
                return Hex.decodeHex(secret.toCharArray());
            } catch (DecoderException e) {
                throw new SecurityException(
                        "The provided string may contain invalid characters or be improperly formatted.");
            }
        }
        return secret.getBytes();
    }

    /**
     * Generated the key id using the secret key.
     *
     * @param secret The plain secret.
     * @return The hashed secret.
     */
    public static String generateKeyId(String secret) {

        byte[] decodedSecret = KeyRotationServiceUtils.determineEncodingAndEncode(secret);
        try {
            MessageDigest digest = MessageDigest.getInstance(DIGEST_ALGORITHM_SHA256);
            return Base64.encode(digest.digest(decodedSecret));
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("Failed to compute hash due to an error." + e.getMessage());
        }
    }
}

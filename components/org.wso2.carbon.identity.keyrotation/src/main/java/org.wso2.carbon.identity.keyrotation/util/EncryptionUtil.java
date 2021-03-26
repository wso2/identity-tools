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
import com.google.gson.JsonParseException;
import org.apache.axiom.om.util.Base64;
import org.wso2.carbon.identity.keyrotation.config.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.model.CipherMetaData;
import org.wso2.carbon.identity.keyrotation.service.CryptoProvider;

import java.nio.charset.Charset;

/**
 * The class that calls the reEncryption mechanism.
 */
public class EncryptionUtil {

    /**
     * ReEncryption mechanism needed for the key rotation task.
     *
     * @param cipher            The ciphertext needed to perform re-encryption on.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return Ciphertext that gets decrypted from the old key and encrypted using the new key.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public static String reEncryptor(String cipher, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        CryptoProvider cryptoProvider = new CryptoProvider();
        byte[] refactoredCipher = cryptoProvider.reFactorCipherText(Base64.decode(cipher));
        byte[] plainText = cryptoProvider.decrypt(refactoredCipher, keyRotationConfig);
        byte[] cipherText = cryptoProvider.encrypt(plainText, keyRotationConfig);
        return Base64.encode(cipherText);
    }

    /**
     * To check if stored field value is encrypted or not.
     *
     * @param fieldValue The field value that needs to be checked whether it is encrypted or not.
     * @return Boolean value.
     */
    public static boolean checkPlainText(String fieldValue) {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String fieldValueStr = new String(Base64.decode(fieldValue), Charset.defaultCharset());
        try {
            CipherMetaData cipherMetaData = gson.fromJson(fieldValueStr, CipherMetaData.class);
            if (cipherMetaData == null) {
                //To capture plaintext data stored in the db through this condition.
                return true;
            }
        } catch (JsonParseException e) {
            //To capture plaintext data stored in the db through this exception.
            return true;
        }
        return false;
    }
}

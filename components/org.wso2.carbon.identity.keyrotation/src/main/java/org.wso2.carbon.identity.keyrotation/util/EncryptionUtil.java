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

import org.apache.axiom.om.util.Base64;
import org.wso2.carbon.identity.keyrotation.config.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.service.CryptoProvider;

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
}
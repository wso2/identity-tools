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
package org.wso2.carbon.identity.keyrotation;

import java.nio.charset.StandardCharsets;

import org.apache.axiom.om.util.Base64;
import org.wso2.carbon.identity.keyrotation.util.CryptoProvider;
import org.wso2.carbon.identity.keyrotation.util.CryptoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class that calls the key-rotation service.
 */
public class KeyRotationService {

    private static final Log log = LogFactory.getLog(KeyRotationService.class);

    public static void main(String[] args) throws Exception {

        try {
            byte[] rm = reEncryptionMechanism();

        } catch (Exception e) {//find a suitable exception to throw
            String errorMessage = "Error " + e;
            throw new Exception(errorMessage, e);
        }
    }

    /**
     * ReEncryption mechanism needed for the key rotation service.
     *
     * @return Decrypted from old key and encrypted from new key.
     * @throws CryptoException Exception which will be thrown if something unexpected happens during crypto operations.
     */
    private static byte[] reEncryptionMechanism() throws CryptoException {

        //todo first must decrypt and then should encrypt
        byte[] cipherText;
        byte[] plainText;
        try {
            CryptoProvider cryptoProvider = new CryptoProvider();
            cipherText =
                    cryptoProvider
                            .encrypt("sampleText".getBytes(StandardCharsets.UTF_8));
            String encodedCipher = Base64.encode(cipherText);
            log.info("Ciphertext: " + encodedCipher);//This gets saved in the DB
            byte[] refactoredCipher = cryptoProvider.reFactorCipherText(Base64.decode(Base64.encode(cipherText)));
            plainText = cryptoProvider.decrypt(refactoredCipher);
            log.info("Plaintext: " + new String(plainText));
        } catch (CryptoException e) {
            String errorMessage = "Error occured while performing crypto operation " + e;
            throw new CryptoException(errorMessage, e);
        }
        return cipherText;
    }
}

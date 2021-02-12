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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.axiom.om.util.Base64;
import org.wso2.carbon.identity.keyrotation.util.CryptoProvider;
import org.wso2.carbon.identity.keyrotation.util.CryptoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.keyrotation.util.MigrationConfig;

/**
 * Class that calls the key-rotation service.
 */
public class KeyRotationService {

    private static final Log log = LogFactory.getLog(KeyRotationService.class);

    public static void main(String[] args) throws Exception {

        try {
            readYAML();
            byte[] rm = reEncryptionMechanism();

        } catch (Exception e) {//find a suitable exception to throw
            String errorMessage = String.format("ERROR " + e);
            throw new Exception(errorMessage, e);
        }
    }

    /**
     * ReEncryption mechanism needed for the key rotation service.
     *
     * @return ReEncrypted value       Decrypted from old key and encrypted from new key.
     * @throws CryptoException         Crypto operation exception.
     */
    private static byte[] reEncryptionMechanism() throws CryptoException {

        byte[] cipherText = new byte[0];
        byte[] plainText = new byte[0];
        try {
            CryptoProvider cryptoProvider = new CryptoProvider();
            cipherText =
                    cryptoProvider
                            .encrypt(new String("sampleText").getBytes(StandardCharsets.UTF_8));
            String encodedCipher = Base64.encode(cipherText);
            log.info("encrypt: " + encodedCipher);//This gets saved in the DB
            byte[] refactoredCipher = cryptoProvider.reFactorCipherText(Base64.decode(Base64.encode(cipherText)));
            plainText = cryptoProvider.decrypt(refactoredCipher);
            log.info("decrypt: " + plainText);
        } catch (CryptoException e) {
            String errorMessage = String.format("CRYPTO ERROR " + e);
            throw new CryptoException(errorMessage, e);
        }
        return cipherText;
    }

    /**
     * Read the properties.yaml file and get the configuration data.
     * @throws IOException          Input/Output operation exception.
     */
    private static void readYAML() throws IOException {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        File file = new File(classLoader.getResource("properties.yaml").getFile());
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        MigrationConfig migrationConfig = om.readValue(file, MigrationConfig.class);
        log.info("MigrationConfig: " + migrationConfig.toString());

    }
}

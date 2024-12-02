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
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.keyrotation.config.model.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.model.CipherMetaData;
import org.wso2.carbon.identity.keyrotation.service.CryptoProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * This class holds the re-encryption mechanism.
 */
public class EncryptionUtil {

    private static final Logger logger = LoggerFactory.getLogger(EncryptionUtil.class);
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    /**
     * Re-encryption mechanism needed for the key rotation task.
     *
     * @param cipher            The ciphertext needed to perform re-encryption on.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return Ciphertext that gets decrypted from the old key and encrypted using the new key.
     * @throws KeyRotationException Exception thrown while performing re-encryption.
     */
    public static String symmetricReEncryption(String cipher, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        CryptoProvider cryptoProvider = new CryptoProvider();
        byte[] refactoredCipher = cryptoProvider.reFactorCipherText(Base64.decode(cipher));
        byte[] plainText = cryptoProvider.decrypt(refactoredCipher, keyRotationConfig);
        // If the plain text is null, it means the cipher text is already encrypted by the new key.
        if (plainText == null) {
            return null;
        }
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

        String fieldValueStr = new String(Base64.decode(fieldValue), Charset.defaultCharset());
        try {
            CipherMetaData cipherMetaData = gson.fromJson(fieldValueStr, CipherMetaData.class);
            if (cipherMetaData == null) {
                // To capture plaintext data stored in the db through this condition.
                return true;
            }
        } catch (JsonParseException e) {
            // To capture plaintext data stored in the db through this exception.
            return true;
        }
        return false;
    }

    /**
     * Generating backup file for the given list of strings of SQL queries.
     *
     * @param regProperty   The database table name.
     * @param backupStrings The list of selected data consist of encrypted secrets.
     */
    public static void writeBackup(String regProperty, List<String> backupStrings) {

        if (CollectionUtils.isEmpty(backupStrings)) {
            logger.error("No data found to backup for: " + regProperty);
            return;
        }

        String homeDirectory = System.getProperty("user.dir");
        // Create a File object for the backup directory
        File backupDir = new File(homeDirectory, "backup");

        // Check if the directory exists, and create it if it does not
        if (!backupDir.exists()) {
            if (backupDir.mkdir()) {
                logger.info("Backup directory created at: " + backupDir.getAbsolutePath());
            } else {
                logger.error("Failed to create backup directory.");
                return;
            }
        }

        // Define the file path (in the home directory)
        File file = new File(backupDir, "backup_" + regProperty + ".sql");

        // Write the list of strings to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            for (String str : backupStrings) {
                writer.write(str);
                writer.newLine();
            }
            logger.info("Backup added for: " + regProperty + " at " + file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error while adding backup for: " + regProperty);
        }

    }
}

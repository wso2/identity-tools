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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.wso2.carbon.identity.keyrotation.config.KeyRotationConfig;
import org.wso2.carbon.uuid.generator.UUIDGeneratorManager;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.wso2.carbon.identity.keyrotation.model.CipherMetaData;

import static org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants.ALGORITHM;
import static org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants.TRANSFORMATION;

/**
 * Class that implements the encryption and decryption tasks.
 */
public class CryptoProvider {

    private static final Log log = LogFactory.getLog(CryptoProvider.class);
    public static final int GCM_IV_LENGTH = 16;
    public static final String JAVA_SECURITY_API_PROVIDER = "BC";

    /**
     * Computes and returns the ciphertext of the given cleartext.
     *
     * @param cleartext The cleartext to be encrypted.
     * @return The encrypted cleartext.
     * @throws CryptoException Exception which will be thrown if something unexpected happens during crypto operations..
     */
    public byte[] encrypt(byte[] cleartext) throws CryptoException {

        log.info("Encrypting data with symmetric key encryption with algorithm AES/GCM/NoPadding");
        if (cleartext == null) throw new CryptoException("Plaintext can't be null.");
        Cipher cipher;
        byte[] cipherText;
        byte[] iv = getInitializationVector();

        try {
            //Add the BC security provider for better security instead of the default provider.
            Security.addProvider(new BouncyCastleProvider());
            cipher = Cipher.getInstance(TRANSFORMATION, JAVA_SECURITY_API_PROVIDER);
            KeyRotationConfig keyRotationConfig = KeyRotationConfig.loadConfigs();
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(keyRotationConfig.getNewSecretKey()),
                    new IvParameterSpec(iv));
            cipherText = cipher.doFinal(cleartext);
            cipherText = createSelfContainedCiphertext(cipherText, iv);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            String errorMessage = String.format("Error occurred while instantiating Cipher object" +
                    " with algorithm: '%s'.", TRANSFORMATION);
            throw new CryptoException(errorMessage, e);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            String errorMessage = String.format("Error occurred while initializing Cipher object" +
                    " with algorithm: '%s'.", TRANSFORMATION);
            throw new CryptoException(errorMessage, e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            String errorMessage = String.format("Error occurred while encrypting using Cipher object" +
                    " with algorithm: '%s'.", TRANSFORMATION);
            throw new CryptoException(errorMessage, e);
        }
        return cipherText;
    }

    /**
     * Computes and returns the cleartext of the given ciphertext.
     *
     * @param cipherText The ciphertext to be decrypted.
     * @return The decrypted ciphertext.
     * @throws CryptoException Exception which will be thrown if something unexpected happens during crypto operations.
     **/
    public byte[] decrypt(byte[] cipherText) throws CryptoException {

        log.info("Decrypting data with symmetric key encryption with algorithm AES/GCM/NoPadding");
        if (cipherText == null && cipherText.length == 0) {
            throw new CryptoException("Plaintext cannot be empty.");
        }
        try {
            CipherMetaData cipherMetaData = createCipherMetaData(cipherText);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION, JAVA_SECURITY_API_PROVIDER);
            KeyRotationConfig keyRotationConfig = KeyRotationConfig.loadConfigs();
            cipher.init(Cipher.DECRYPT_MODE,
                    getSecretKey(keyRotationConfig.getOldSecretKey()),
                    new IvParameterSpec(cipherMetaData.getIvBase64Decoded()));
            return cipher.doFinal(cipherMetaData.getCipherBase64Decoded());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            String errorMessage = String.format("Error occurred while instantiating Cipher object" +
                    " with algorithm: '%s'.", TRANSFORMATION);
            throw new CryptoException(errorMessage, e);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            String errorMessage = String.format("Error occurred while initializing Cipher object" +
                    " with algorithm: '%s'.", TRANSFORMATION);
            throw new CryptoException(errorMessage, e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            String errorMessage = String.format("Error occurred while encrypting using Cipher object" +
                    " with algorithm: '%s'.", TRANSFORMATION);
            throw new CryptoException(errorMessage, e);
        }
    }

    /**
     * Returns the raw secret key as a byte array.
     *
     * @param secretKey The data encryption key.
     * @return Secret key.
     */
    private SecretKeySpec getSecretKey(String secretKey) {

        return new SecretKeySpec(secretKey.getBytes(), 0, secretKey.getBytes().length,
                ALGORITHM);
    }

    /**
     * Creates and returns a universally unique identifier for the IV.
     *
     * @return Initialization vector used for encryption/decryption.
     */
    private byte[] getInitializationVector() {

        byte[] iv = new byte[GCM_IV_LENGTH];
        UUID timeBasedUUID = UUIDGeneratorManager.getTimeBasedUUIDGenerator().generate();
        ByteBuffer byteBuffer = ByteBuffer.wrap(iv);
        byteBuffer.putLong(timeBasedUUID.getMostSignificantBits());
        byteBuffer.putLong(timeBasedUUID.getLeastSignificantBits());
        return byteBuffer.array();
    }

    /**
     * Creates and returns a self contained ciphertext with IV.
     *
     * @param originalCipher The ciphertext.
     * @param iv             The Initialization Vector.
     * @return Self contained meta data comprising of the cipher, transformation and IV.
     */
    private byte[] createSelfContainedCiphertext(byte[] originalCipher, byte[] iv) {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        CipherMetaData cipherMetaData = new CipherMetaData();
        cipherMetaData.setCipherText(KeyRotationServiceUtils.getSelfContainedCiphertextWithIv(originalCipher, iv));
        cipherMetaData.setTransformation(TRANSFORMATION);
        cipherMetaData.setIv(Base64.encode(iv));
        String cipherWithMetadataStr = gson.toJson(cipherMetaData);
        return cipherWithMetadataStr.getBytes(Charset.defaultCharset());
    }

    /**
     * Returns the self contained cipherText with IV.
     *
     * @param cipherTextBytes The ciphertext.
     * @return Self contained meta data comprising of the cipher and IV.
     */
    private CipherMetaData createCipherMetaData(byte[] cipherTextBytes) {

        return KeyRotationServiceUtils.setIvAndOriginalCipherText(cipherTextBytes);
    }

    /**
     * Returns the refactored encrypted ciphertext needed for the decryption method.
     *
     * @param cipherText The ciphertext.
     * @return Refactored cipher The refactored ciphertext.
     */
    public byte[] reFactorCipherText(byte[] cipherText) {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String cipherStr = new String(cipherText, Charset.defaultCharset());
        CipherMetaData cipherMetaData = gson.fromJson(cipherStr, CipherMetaData.class);
        cipherText = cipherMetaData.getCipherBase64Decoded();
        return cipherText;
    }
}

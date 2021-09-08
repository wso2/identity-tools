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

package org.wso2.carbon.identity.keyrotation.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.wso2.carbon.identity.keyrotation.config.model.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.model.CipherMetaData;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationServiceUtils;
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

/**
 * This class holds implementations of the encryption and decryption tasks.
 */
public class CryptoProvider {

    private static final Logger log = Logger.getLogger(CryptoProvider.class);
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    /**
     * Computes and returns the ciphertext of the given cleartext.
     *
     * @param cleartext         The cleartext to be encrypted.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return The encrypted cleartext.
     * @throws KeyRotationException Exception thrown while encrypting the cleartext.
     */
    public byte[] encrypt(byte[] cleartext, KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        if (cleartext == null) {
            throw new KeyRotationException("Cleartext bytes cannot be null.");
        }
        Cipher cipher;
        byte[] cipherText;
        byte[] iv = getInitializationVector();

        try {
            // Add the BC security provider for better security instead of the default provider.
            Security.addProvider(new BouncyCastleProvider());
            cipher = Cipher.getInstance(KeyRotationConstants.TRANSFORMATION,
                    KeyRotationConstants.JAVA_SECURITY_API_PROVIDER);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(keyRotationConfig.getNewSecretKey()),
                    new IvParameterSpec(iv));
            cipherText = cipher.doFinal(cleartext);
            cipherText = createSelfContainedCiphertext(cipherText, iv);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            String errorMessage = String.format("Error occurred while instantiating cipher object" +
                    " with algorithm: '%s'.", KeyRotationConstants.TRANSFORMATION);
            throw new KeyRotationException(errorMessage, e);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            String errorMessage = String.format("Error occurred while initializing cipher object" +
                    " with algorithm: '%s'.", KeyRotationConstants.TRANSFORMATION);
            throw new KeyRotationException(errorMessage, e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            String errorMessage = String.format("Error occurred while encrypting using cipher object" +
                    " with algorithm: '%s'.", KeyRotationConstants.TRANSFORMATION);
            throw new KeyRotationException(errorMessage, e);
        }
        return cipherText;
    }

    /**
     * Computes and returns the cleartext of the given ciphertext.
     *
     * @param cipherText        The ciphertext to be decrypted.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return The decrypted ciphertext.
     * @throws KeyRotationException Exception thrown while decrypting the ciphertext.
     **/
    public byte[] decrypt(byte[] cipherText, KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        if (cipherText == null) {
            throw new KeyRotationException("Ciphertext bytes cannot be null.");
        }
        Cipher cipher;
        try {
            // Add the BC security provider for better security instead of the default provider.
            Security.addProvider(new BouncyCastleProvider());
            CipherMetaData cipherMetaData = createCipherMetaData(cipherText);
            // This check is for empty bytes of data that was encrypted and stored.
            if (cipherMetaData.getCipherBase64Decoded().length == 0) {
                log.debug("Bytes of length 0 found for cipher within the cipherMetaData.");
                return StringUtils.EMPTY.getBytes();
            }
            cipher = Cipher.getInstance(KeyRotationConstants.TRANSFORMATION,
                    KeyRotationConstants.JAVA_SECURITY_API_PROVIDER);
            cipher.init(Cipher.DECRYPT_MODE,
                    getSecretKey(keyRotationConfig.getOldSecretKey()),
                    new IvParameterSpec(cipherMetaData.getIvBase64Decoded()));
            return cipher.doFinal(cipherMetaData.getCipherBase64Decoded());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            String errorMessage = String.format("Error occurred while instantiating cipher object" +
                    " with algorithm: '%s'.", KeyRotationConstants.TRANSFORMATION);
            throw new KeyRotationException(errorMessage, e);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            String errorMessage = String.format("Error occurred while initializing cipher object" +
                    " with algorithm: '%s'.", KeyRotationConstants.TRANSFORMATION);
            throw new KeyRotationException(errorMessage, e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            String errorMessage = String.format("Error occurred while encrypting using cipher object" +
                    " with algorithm: '%s'.", KeyRotationConstants.TRANSFORMATION);
            throw new KeyRotationException(errorMessage, e);
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
                KeyRotationConstants.ALGORITHM);
    }

    /**
     * Creates and returns a universally unique identifier for the IV.
     *
     * @return Initialization vector used for encryption/decryption.
     */
    private byte[] getInitializationVector() {

        byte[] iv = new byte[KeyRotationConstants.GCM_IV_LENGTH];
        UUID timeBasedUUID = UUIDGeneratorManager.getTimeBasedUUIDGenerator().generate();
        ByteBuffer byteBuffer = ByteBuffer.wrap(iv);
        byteBuffer.putLong(timeBasedUUID.getMostSignificantBits());
        byteBuffer.putLong(timeBasedUUID.getLeastSignificantBits());
        return byteBuffer.array();
    }

    /**
     * Creates and returns a self contained ciphertext with IV.
     *
     * @param cipherText The ciphertext.
     * @param iv         The Initialization Vector.
     * @return Self contained meta data comprising of the cipher, transformation and IV.
     */
    private byte[] createSelfContainedCiphertext(byte[] cipherText, byte[] iv) {

        CipherMetaData cipherMetaData = new CipherMetaData();
        cipherMetaData.setCipherText(KeyRotationServiceUtils.getSelfContainedCiphertextWithIv(cipherText, iv));
        cipherMetaData.setTransformation(KeyRotationConstants.TRANSFORMATION);
        cipherMetaData.setInitializationVector(Base64.encode(iv));
        String cipherWithMetadataStr = gson.toJson(cipherMetaData);
        return cipherWithMetadataStr.getBytes(Charset.defaultCharset());
    }

    /**
     * Returns the self contained cipherText with IV.
     *
     * @param cipherText The ciphertext.
     * @return Self contained meta data comprising of the cipher and IV.
     */
    private CipherMetaData createCipherMetaData(byte[] cipherText) {

        return KeyRotationServiceUtils.setIvAndOriginalCipherText(cipherText);
    }

    /**
     * Returns the refactored encrypted ciphertext needed for the decryption method.
     *
     * @param cipherText The ciphertext.
     * @return Refactored cipher The refactored ciphertext.
     * @throws KeyRotationException Exception thrown while re-factoring the cipher text.
     */
    public byte[] reFactorCipherText(byte[] cipherText) throws KeyRotationException {

        try {
            String cipherStr = new String(cipherText, Charset.defaultCharset());
            CipherMetaData cipherMetaData = gson.fromJson(cipherStr, CipherMetaData.class);
            cipherText = cipherMetaData.getCipherBase64Decoded();
        } catch (JsonSyntaxException e) {
            throw new KeyRotationException("Error occurred while converting JSON to a Java object, ", e);
        }
        return cipherText;
    }
}

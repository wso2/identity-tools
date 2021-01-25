package org.wso2.carbon.identity.keyrotation.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.uuid.generator.UUIDGeneratorManager;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SymmetricKeyInternalCryptoProvider {

    private static String secretKey = "AFA27B44D43B02A9FEA41D13CEDC2E40";
    private static final String DEFAULT_SYMMETRIC_CRYPTO_ALGORITHM = "AES";
    private static final String AES_GCM_SYMMETRIC_CRYPTO_ALGORITHM = "AES/GCM/NoPadding";
    public static final int GCM_IV_LENGTH = 16;
    public static final int GCM_TAG_LENGTH = 128;

    public static byte[] encrypt(byte[] cleartext, String algorithm, String javaSecurityAPIProvider,
                          boolean returnSelfContainedCipherText) throws CryptoException {


        System.out.println(String.format("Encrypting data with symmetric key encryption with algorithm: '%s'.",
        algorithm));

        byte[] cipherText;
        if (cleartext == null) {
            throw new CryptoException("Plaintext can't be null.");
        }
        if (AES_GCM_SYMMETRIC_CRYPTO_ALGORITHM.equals(algorithm)) {
            return encryptWithGCMMode(cleartext, javaSecurityAPIProvider, returnSelfContainedCipherText);
        }
        if (StringUtils.isNotBlank(algorithm) && cleartext.length == 0) {

            System.out.println("Plaintext is empty. An empty array will be used as the ciphertext bytes.");

            cipherText = StringUtils.EMPTY.getBytes();
            if (returnSelfContainedCipherText) {
                return createSelfContainedCiphertextWithPlainAES(cipherText, algorithm);
            } else {
                return cipherText;
            }
        }
        if (returnSelfContainedCipherText) {
            cipherText = encrypt(cleartext, algorithm, javaSecurityAPIProvider);
            return createSelfContainedCiphertextWithPlainAES(cipherText, algorithm);
        }
        return encrypt(cleartext, algorithm, javaSecurityAPIProvider);

    }

    public static byte[] encrypt(byte[] cleartext, String algorithm, String javaSecurityAPIProvider) throws CryptoException {

        try {
            Cipher cipher;
            if (StringUtils.isBlank(algorithm)) {
                algorithm = AES_GCM_SYMMETRIC_CRYPTO_ALGORITHM;
            }
            if (StringUtils.isBlank(javaSecurityAPIProvider)) {
                cipher = Cipher.getInstance(algorithm);
            } else {
                cipher = Cipher.getInstance(algorithm, javaSecurityAPIProvider);
            }

            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            return cipher.doFinal(cleartext);
        } catch (InvalidKeyException | NoSuchPaddingException | BadPaddingException | NoSuchProviderException
                | IllegalBlockSizeException | NoSuchAlgorithmException e) {
            String errorMessage = String.format("An error occurred while encrypting using the algorithm : '%s'"
                    , algorithm);

            // Log the exception from client libraries, to avoid missing information if callers code doesn't log it

            System.out.println(errorMessage+e);


            throw new CryptoException(errorMessage, e);
        }
    }

    private static byte[] encryptWithGCMMode(byte[] plaintext, String javaSecurityAPIProvider,
                                      boolean returnSelfContainedCipherText)
            throws CryptoException {

        Cipher cipher;
        byte[] cipherText;
        if (!returnSelfContainedCipherText) {
            throw new CryptoException("Symmetric encryption with GCM mode only supports self contained cipher " +
                    "text generation.");

        }
        byte[] iv = getInitializationVector();
        try {
            if (StringUtils.isBlank(javaSecurityAPIProvider)) {
                cipher = Cipher.getInstance(AES_GCM_SYMMETRIC_CRYPTO_ALGORITHM);
            } else {
                cipher = Cipher.getInstance(AES_GCM_SYMMETRIC_CRYPTO_ALGORITHM, javaSecurityAPIProvider);
            }
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), getGCMParameterSpec(iv));
            if (plaintext.length == 0) {

                System.out.println("Plaintext is empty. An empty array will be used as the ciphertext bytes.");

                cipherText = StringUtils.EMPTY.getBytes();
            } else {
                cipherText = cipher.doFinal(plaintext);
            }
            cipherText = createSelfContainedCiphertextWithGCMMode(cipherText, AES_GCM_SYMMETRIC_CRYPTO_ALGORITHM, iv);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchProviderException e) {

            String errorMessage = String.format("Error occurred while initializing and encrypting using Cipher object" +
                    " with algorithm: '%s'.", AES_GCM_SYMMETRIC_CRYPTO_ALGORITHM);
            throw new CryptoException(errorMessage, e);
        }
        return cipherText;
    }

    public static byte[] decrypt(byte[] ciphertext, String algorithm, String javaSecurityAPIProvider) throws CryptoException {

        try {
            Cipher cipher;

            if (StringUtils.isBlank(algorithm)) {
                algorithm = AES_GCM_SYMMETRIC_CRYPTO_ALGORITHM;
            }
            if (StringUtils.isBlank(javaSecurityAPIProvider)) {
                cipher = Cipher.getInstance(algorithm);
            } else {
                cipher = Cipher.getInstance(algorithm, javaSecurityAPIProvider);
            }
            if (AES_GCM_SYMMETRIC_CRYPTO_ALGORITHM.equals(algorithm)) {

                System.out.println(String.format("Decrypting internal data with '%s' algorithm.", algorithm));

                CipherMetaDataHolder cipherMetaDataHolder = getCipherMetaDataHolderFromCipherText(ciphertext);
                cipher.init(Cipher.DECRYPT_MODE, getSecretKey(),
                        getGCMParameterSpec(cipherMetaDataHolder.getIvBase64Decoded()));
                if (cipherMetaDataHolder.getCipherBase64Decoded().length == 0) {
                    return StringUtils.EMPTY.getBytes();
                } else {
                    return cipher.doFinal(cipherMetaDataHolder.getCipherBase64Decoded());
                }

            } else {
                cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            }

            return cipher.doFinal(ciphertext);
        } catch (InvalidKeyException | NoSuchPaddingException | BadPaddingException | NoSuchProviderException | IllegalBlockSizeException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            String errorMessage = String.format("An error occurred while decrypting using the algorithm : '%s'"
                    , algorithm);

            // Log the exception from client libraries, to avoid missing information if callers code doesn't log it

            System.out.println(errorMessage+ e);


            throw new CryptoException(errorMessage, e);
        }
    }

    private static SecretKeySpec getSecretKey() {

        return new SecretKeySpec(secretKey.getBytes(), 0, secretKey.getBytes().length,
                DEFAULT_SYMMETRIC_CRYPTO_ALGORITHM);
    }

    private static byte[] getInitializationVector() {

        byte[] iv = new byte[GCM_IV_LENGTH];
        UUID timeBasedUUID = UUIDGeneratorManager.getTimeBasedUUIDGenerator().generate();
        ByteBuffer byteBuffer = ByteBuffer.wrap(iv);
        byteBuffer.putLong(timeBasedUUID.getMostSignificantBits());
        byteBuffer.putLong(timeBasedUUID.getLeastSignificantBits());
        return byteBuffer.array();
    }

    private static byte[] createSelfContainedCiphertextWithPlainAES(byte[] originalCipher, String transformation) {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        CipherMetaDataHolder cipherHolder = new CipherMetaDataHolder();
        cipherHolder.setCipherText(Base64.encode(originalCipher));
        cipherHolder.setTransformation(transformation);
        String cipherWithMetadataStr = gson.toJson(cipherHolder);

        System.out.println("Cipher with meta data: " + cipherWithMetadataStr);

        return cipherWithMetadataStr.getBytes(Charset.defaultCharset());
    }

    private static byte[] createSelfContainedCiphertextWithGCMMode(byte[] originalCipher, String transformation,
                                                                   byte[] iv) {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        CipherMetaDataHolder cipherHolder = new CipherMetaDataHolder();
        cipherHolder.setCipherText(Base64.encode(cipherHolder.getSelfContainedCiphertextWithIv(originalCipher, iv)));
        cipherHolder.setTransformation(transformation);
        cipherHolder.setIv(Base64.encode(iv));
        String cipherWithMetadataStr = gson.toJson(cipherHolder);

        System.out.println("Cipher with meta data : " + cipherWithMetadataStr);

        return cipherWithMetadataStr.getBytes(Charset.defaultCharset());
    }

    private static GCMParameterSpec getGCMParameterSpec(byte[] iv) {

        //The GCM parameter authentication tag length we choose is 128.
        return new GCMParameterSpec(GCM_TAG_LENGTH, iv);
    }

    private static CipherMetaDataHolder getCipherMetaDataHolderFromCipherText(byte[] cipherTextBytes) {

        CipherMetaDataHolder cipherMetaDataHolder = new CipherMetaDataHolder();
        cipherMetaDataHolder.setIvAndOriginalCipherText(cipherTextBytes);
        return cipherMetaDataHolder;
    }

}

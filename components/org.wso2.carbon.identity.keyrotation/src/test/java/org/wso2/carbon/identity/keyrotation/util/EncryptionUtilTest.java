package org.wso2.carbon.identity.keyrotation.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.keyrotation.config.model.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.model.CipherInitializationVector;
import org.wso2.carbon.identity.keyrotation.model.CipherMetaData;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Properties;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class EncryptionUtilTest {

    private static final Logger logger = LoggerFactory.getLogger(EncryptionUtilTest.class);
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private static final String NEW_SECRET = "b988c775c435f05c3da1fbecfe76c1e7";
    private static final String OLD_SECRET = "9501cb661d04b0dbe68d17bff2060737";
    private static final String OLD_SECRET_2 = "b5d030394142dda37b22997dbcc5640c";

    private static String encryptedAuthorizationCodeOldWithoutKeyId;
    private static String encryptedAuthorizationCodeOld2WithKeyId;
    private static String encryptedAuthorizationCodeNewWithKeyId;

    Properties encryptedSecrets;

    @Mock
    KeyRotationConfig keyRotationConfig;

    @BeforeClass
    public void init() {

        MockitoAnnotations.openMocks(this); // Initialize mocks
        loadEncryptedSecrets();
        encryptedAuthorizationCodeOldWithoutKeyId =
                (String) encryptedSecrets.get("encryptedAuthorizationCodeOldWithoutKeyId");
        encryptedAuthorizationCodeOld2WithKeyId =
                (String) encryptedSecrets.get("encryptedAuthorizationCodeOld2WithKeyId");
        encryptedAuthorizationCodeNewWithKeyId =
                (String) encryptedSecrets.get("encryptedAuthorizationCodeNewWithKeyId");
    }

    @Test(description = "Rotating via the existing same secret.")
    public void testRotateViaExistingSecret() {

        when(keyRotationConfig.getOldSecretKey()).thenReturn(NEW_SECRET);
        when(keyRotationConfig.getNewSecretKey()).thenReturn(NEW_SECRET);

        String reEncryptedText;
        try {
            reEncryptedText = EncryptionUtil.symmetricReEncryption(encryptedAuthorizationCodeNewWithKeyId,
                    keyRotationConfig);
            assertNull(reEncryptedText);

        } catch (KeyRotationException e) {
            logger.error("Error while encrypting the test data", e);
            Assert.fail();
        }
    }

    @Test(description = "Rotating via a new secret.")
    public void testRotateViaANewSecret() {

        when(keyRotationConfig.getOldSecretKey()).thenReturn(OLD_SECRET);
        when(keyRotationConfig.getNewSecretKey()).thenReturn(NEW_SECRET);

        String reEncryptedText;
        try {
            reEncryptedText = EncryptionUtil.symmetricReEncryption(encryptedAuthorizationCodeOldWithoutKeyId,
                    keyRotationConfig);
            assertNotNull(reEncryptedText);

            try {
                String decodedEncryptedText = new String(Base64.decode(reEncryptedText), Charset.defaultCharset());
                CipherMetaData cipherMetaData = gson.fromJson(decodedEncryptedText, CipherMetaData.class);

                if (StringUtils.isNotBlank(cipherMetaData.getCipherText())) {

                    String decodedCipher =
                            new String(cipherMetaData.getCipherBase64Decoded(), Charset.defaultCharset());
                    CipherInitializationVector cipherInitializationVector =
                            gson.fromJson(decodedCipher, CipherInitializationVector.class);

                    String kid = KeyRotationServiceUtils.generateKeyId(NEW_SECRET);

                    assertEquals(cipherInitializationVector.getKeyId(), kid);
                }

            } catch (JsonSyntaxException e) {
                logger.error("Error occurred while converting JSON to a Java object.", e);
                Assert.fail();
            }
        } catch (KeyRotationException e) {
            logger.error("Error while encrypting the test secrets", e);
            Assert.fail();
        }
    }

    @Test(description = "Rotating via a new secret.")
    public void testRotateViaASecret() {

        when(keyRotationConfig.getOldSecretKey()).thenReturn(OLD_SECRET_2);
        when(keyRotationConfig.getNewSecretKey()).thenReturn(NEW_SECRET);

        String reEncryptedText;
        try {
            reEncryptedText = EncryptionUtil.symmetricReEncryption(encryptedAuthorizationCodeOld2WithKeyId,
                    keyRotationConfig);

            try {
                String decodedEncryptedText = new String(Base64.decode(reEncryptedText), Charset.defaultCharset());
                CipherMetaData cipherMetaData = gson.fromJson(decodedEncryptedText, CipherMetaData.class);

                if (StringUtils.isNotBlank(cipherMetaData.getCipherText())) {

                    String decodedCipher =
                            new String(cipherMetaData.getCipherBase64Decoded(), Charset.defaultCharset());
                    CipherInitializationVector cipherInitializationVector =
                            gson.fromJson(decodedCipher, CipherInitializationVector.class);

                    String kid = KeyRotationServiceUtils.generateKeyId(NEW_SECRET);

                    assertEquals(cipherInitializationVector.getKeyId(), kid);
                }

            } catch (JsonSyntaxException e) {
                logger.error("Error occurred while converting JSON to a Java object.", e);
                Assert.fail();
            }
        } catch (KeyRotationException e) {
            logger.error("Error while encrypting the test secrets", e);
            Assert.fail();
        }
    }

    void loadEncryptedSecrets() {

        encryptedSecrets = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("encrypted.properties")) {
            Assert.assertNotNull(input);
            encryptedSecrets.load(input);
        } catch (IOException ex) {
            logger.error("Error while loading the encrypted test properties from the file: encrypted.properties", ex);
            Assert.fail();
        }
    }
}

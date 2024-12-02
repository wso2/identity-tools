package org.wso2.carbon.identity.keyrotation.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.wso2.carbon.identity.keyrotation.config.model.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.model.CipherInitializationVector;
import org.wso2.carbon.identity.keyrotation.model.CipherMetaData;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationServiceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseSecretTest implements DataQueries {

    private static final Logger logger = LoggerFactory.getLogger(BaseSecretTest.class);
    private static final String NEW_SECRET = "b988c775c435f05c3da1fbecfe76c1e7";
    private static final String OLD_SECRET = "9501cb661d04b0dbe68d17bff2060737";
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    public static final String DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "";

    static KeyRotationConfig keyRotationConfig;
    static Properties encryptedSecrets;

    String createTableQuery;
    String insertDataQuery;

    @BeforeAll
    void init() {
        // Prepare key rotation config
        keyRotationConfig = new KeyRotationConfig();
        keyRotationConfig.setOldSecretKey(OLD_SECRET);
        keyRotationConfig.setNewSecretKey(NEW_SECRET);
        keyRotationConfig.setIdnDBUrl(DB_URL);
        keyRotationConfig.setIdnUsername(USERNAME);
        keyRotationConfig.setIdnPassword(PASSWORD);
        keyRotationConfig.setRegUsername(USERNAME);
        keyRotationConfig.setRegPassword(PASSWORD);
        keyRotationConfig.setRegDBUrl(DB_URL);
        logger.info("Initialized keyRotationConfig for test scope");

        setChunkSize();
        loadEncryptedSecrets();
        setDatabaseQueries();
        setupDatabase();
    }

    /**
     * This method creates the test database tables and insert data which are given at each child classes.
     */
    private void setupDatabase() {

        try (Connection connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.execute(this.createTableQuery);
            if (StringUtils.isNotBlank(insertDataQuery)) {
                statement.execute(this.insertDataQuery);
            }
        } catch (SQLException e) {
            logger.error("Executing the test db scripts", e);
            Assert.fail();
        }
    }

    @Test
    void testSecretReEncryption() throws KeyRotationException {

        performAssertions();
    }

    /**
     * Common assertion method for all child classes with common structure.
     * This basically decode reEncryptedKeyId and
     *
     * @param reEncryptedSecret The newly encrypted secret.
     */
    void assertNewKeyId(String reEncryptedSecret) {

        try {
            String decodedEncryptedText = new String(Base64.decode(reEncryptedSecret), Charset.defaultCharset());
            CipherMetaData cipherMetaData = gson.fromJson(decodedEncryptedText, CipherMetaData.class);

            if (StringUtils.isNotBlank(cipherMetaData.getCipherText())) {

                String decodedCipher =
                        new String(cipherMetaData.getCipherBase64Decoded(), Charset.defaultCharset());
                CipherInitializationVector cipherInitializationVector =
                        gson.fromJson(decodedCipher, CipherInitializationVector.class);
                String kid = KeyRotationServiceUtils.generateKeyId(NEW_SECRET);

                Assert.assertEquals(cipherInitializationVector.getKeyId(), kid);
            }

        } catch (JsonSyntaxException e) {
            logger.error("Error while generating the object from JSON string", e);
            Assert.fail();
        }
    }

    /**
     * This method loads the file encrypted.properties which consists of the all the required encrypted secrets related
     * the tests.
     */
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

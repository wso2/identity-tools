package org.wso2.carbon.identity.keyrotation.dao;

import org.wso2.carbon.identity.keyrotation.model.OAuthSecret;
import org.wso2.carbon.identity.keyrotation.service.DBKeyRotator;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IdnOAuthConsumerAppsSecretsTest extends BaseSecretTest {

    @Override
    public void setDatabaseQueries() {

        this.createTableQuery = "CREATE TABLE IDN_OAUTH_CONSUMER_APPS (" +
                "ID INTEGER NOT NULL AUTO_INCREMENT, " +
                "CONSUMER_SECRET CHARACTER VARYING(2048), " +
                "APP_NAME CHARACTER VARYING(255)," +
                "CONSTRAINT CONSUMER_PRIMARY_KEY PRIMARY KEY (ID));";
        this.insertDataQuery = "INSERT INTO IDN_OAUTH_CONSUMER_APPS (CONSUMER_SECRET, APP_NAME) " +
                "VALUES ('" + encryptedSecrets.get("consumerAppSecret") + "', 'CallMe')" +
                ", ('" + encryptedSecrets.get("consumerAppSecret") + "', 'CallA')" +
                ", ('" + encryptedSecrets.get("consumerAppSecret") + "', 'CallB')" +
                ", ('" + encryptedSecrets.get("consumerAppSecret") + "', 'CallC')" +
                ", ('" + encryptedSecrets.get("consumerAppSecret") + "', 'CallD');";
    }

    @Override
    public void setChunkSize() {

        keyRotationConfig.setChunkSize(2);
    }

    @Override
    public void performAssertions() throws KeyRotationException {

        DBKeyRotator.getInstance().reEncryptOauthConsumerData(keyRotationConfig);
        List<OAuthSecret> reEncryptedSecrets = OAuthDAO.getInstance().getOAuthSecretChunks(0, keyRotationConfig);

        assertEquals(2, reEncryptedSecrets.size());

        assertEquals("1", reEncryptedSecrets.get(0).getId());
        assertNewKeyId(reEncryptedSecrets.get(0).getConsumerSecret());
        assertEquals("CallMe", reEncryptedSecrets.get(0).getAppName());
    }

}

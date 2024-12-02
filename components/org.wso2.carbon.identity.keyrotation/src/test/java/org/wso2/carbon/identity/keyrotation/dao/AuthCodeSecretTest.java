package org.wso2.carbon.identity.keyrotation.dao;

import org.wso2.carbon.identity.keyrotation.model.OAuthCode;
import org.wso2.carbon.identity.keyrotation.service.DBKeyRotator;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthCodeSecretTest extends BaseSecretTest {

    @Override
    public void setDatabaseQueries() {

        this.createTableQuery = "CREATE TABLE IDN_OAUTH2_AUTHORIZATION_CODE ( " +
                "  CODE_ID CHARACTER VARYING(255) NOT NULL, " +
                "  AUTHORIZATION_CODE CHARACTER VARYING(2048), " +
                "  CONSUMER_KEY_ID INTEGER, " +
                "  CONSTRAINT CONSTRAINT_56 PRIMARY KEY (CODE_ID));";
        this.insertDataQuery = "INSERT INTO PUBLIC.IDN_OAUTH2_AUTHORIZATION_CODE " +
                "(CODE_ID, AUTHORIZATION_CODE, CONSUMER_KEY_ID) " +
                "VALUES('934d8491-c8fa-4408-aa4b-e4737f8c36f9', '" +
                encryptedSecrets.get("authorizationCode") + "', 2);";
    }

    @Override
    public void setChunkSize() {

        keyRotationConfig.setChunkSize(1);
    }

    @Override
    public void performAssertions() throws KeyRotationException {

        DBKeyRotator.getInstance().reEncryptOauthAuthData(keyRotationConfig);
        List<OAuthCode> reEncryptedSecrets = OAuthDAO.getInstance().getOAuthCodeChunks(0, keyRotationConfig);

        assertEquals(1, reEncryptedSecrets.size());

        assertEquals("934d8491-c8fa-4408-aa4b-e4737f8c36f9", reEncryptedSecrets.get(0).getCodeId());
        assertEquals("2", reEncryptedSecrets.get(0).getConsumerKeyId());
        assertNewKeyId(reEncryptedSecrets.get(0).getAuthorizationCode());
    }
}

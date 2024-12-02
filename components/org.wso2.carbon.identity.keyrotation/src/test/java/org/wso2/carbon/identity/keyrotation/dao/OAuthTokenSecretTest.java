package org.wso2.carbon.identity.keyrotation.dao;

import org.wso2.carbon.identity.keyrotation.model.OAuthToken;
import org.wso2.carbon.identity.keyrotation.service.DBKeyRotator;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OAuthTokenSecretTest extends BaseSecretTest {

    @Override
    public void setDatabaseQueries() {

        this.createTableQuery = "CREATE TABLE IDN_OAUTH2_ACCESS_TOKEN (   " +
                " TOKEN_ID CHARACTER VARYING(255) NOT NULL,   " +
                " ACCESS_TOKEN CHARACTER VARYING(2048),   " +
                " REFRESH_TOKEN CHARACTER VARYING(2048),   " +
                " CONSUMER_KEY_ID INTEGER, " +
                " CONSTRAINT CONSTRAINT_F PRIMARY KEY (TOKEN_ID));";
        this.insertDataQuery = "INSERT INTO IDN_OAUTH2_ACCESS_TOKEN " +
                "(TOKEN_ID, ACCESS_TOKEN, REFRESH_TOKEN, CONSUMER_KEY_ID) " +
                "VALUES('b989846a-c1ae-48a3-ab87-d22f103ffde7', '" + encryptedSecrets.get("accessToken") + "','" +
                encryptedSecrets.get("refreshToken") + "', '2');";
    }

    @Override
    public void setChunkSize() {

        keyRotationConfig.setChunkSize(1);
    }

    @Override
    public void performAssertions() throws KeyRotationException {

        DBKeyRotator.getInstance().reEncryptOauthTokenData(keyRotationConfig);

        List<OAuthToken> reEncryptedSecrets = OAuthDAO.getInstance().getOAuthTokenChunks(0, keyRotationConfig);

        assertEquals(1, reEncryptedSecrets.size());
        assertEquals("b989846a-c1ae-48a3-ab87-d22f103ffde7", reEncryptedSecrets.get(0).getTokenId());
        assertNewKeyId(reEncryptedSecrets.get(0).getAccessToken());
        assertNewKeyId(reEncryptedSecrets.get(0).getRefreshToken());
    }
}

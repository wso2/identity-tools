package org.wso2.carbon.identity.keyrotation.dao;

import org.wso2.carbon.identity.keyrotation.model.TOTPSecret;
import org.wso2.carbon.identity.keyrotation.service.DBKeyRotator;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TOTPSecretTest extends BaseSecretTest {

    @Override
    public void setDatabaseQueries() {

        this.createTableQuery = "CREATE TABLE IDN_IDENTITY_USER_DATA ( " +
                " TENANT_ID INTEGER DEFAULT -1234 NOT NULL, " +
                " USER_NAME CHARACTER VARYING(255) NOT NULL, " +
                " DATA_KEY CHARACTER VARYING(255) NOT NULL, " +
                " DATA_VALUE CHARACTER VARYING(2048), " +
                " CONSTRAINT CONSTRAINT_F7 PRIMARY KEY (USER_NAME,TENANT_ID,DATA_KEY) " +
                ");";
        this.insertDataQuery = "INSERT INTO IDN_IDENTITY_USER_DATA " +
                "(TENANT_ID, USER_NAME, DATA_KEY, DATA_VALUE) " +
                "VALUES(-1234, 'userA', 'http://wso2.org/claims/identity/secretkey', '" +
                encryptedSecrets.get("totp") + "');";
    }

    @Override
    public void setChunkSize() {

        keyRotationConfig.setChunkSize(1);
    }

    @Override
    public void performAssertions() throws KeyRotationException {

        DBKeyRotator.getInstance().reEncryptIdentityTOTPData(keyRotationConfig);
        List<TOTPSecret> reEncryptedSecrets = IdentityDAO.getInstance().getTOTPSecretsChunks(0, keyRotationConfig);

        assertEquals(1, reEncryptedSecrets.size());
        assertEquals("-1234", reEncryptedSecrets.get(0).getTenantId());
        assertEquals("userA", reEncryptedSecrets.get(0).getUsername());
        assertEquals("http://wso2.org/claims/identity/secretkey", reEncryptedSecrets.get(0).getDataKey());
        assertNewKeyId(reEncryptedSecrets.get(0).getDataValue());
    }
}

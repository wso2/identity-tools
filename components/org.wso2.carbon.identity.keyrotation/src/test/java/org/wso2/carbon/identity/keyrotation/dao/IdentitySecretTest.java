package org.wso2.carbon.identity.keyrotation.dao;

import org.wso2.carbon.identity.keyrotation.model.IdentitySecret;
import org.wso2.carbon.identity.keyrotation.service.DBKeyRotator;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IdentitySecretTest extends BaseSecretTest {

    @Override
    public void setDatabaseQueries() {

        this.createTableQuery = "CREATE TABLE IDN_SECRET (" +
                "ID CHARACTER VARYING(255) NOT NULL, " +
                "SECRET_VALUE CHARACTER VARYING(8000) NOT NULL, " +
                "CONSTRAINT IDN_SECRET_PRIMARY_KEY PRIMARY KEY (ID));";
        this.insertDataQuery = "INSERT INTO IDN_SECRET (ID,SECRET_VALUE) " +
                "VALUES ('855511fd-10ae-4dbe-979e-6c9a606ba490','" + encryptedSecrets.get("identitySecret") + "')";
    }

    @Override
    public void setChunkSize() {

        keyRotationConfig.setChunkSize(1);
    }

    @Override
    public void performAssertions() throws KeyRotationException {

        DBKeyRotator.getInstance().reEncryptIdentitySecrets(keyRotationConfig);
        List<IdentitySecret> reEncryptedSecrets =
                IdentityDAO.getInstance().getIdentitySecretChunks(0, keyRotationConfig);
        // Assertions
        assertEquals(1, reEncryptedSecrets.size());

        assertEquals("855511fd-10ae-4dbe-979e-6c9a606ba490", reEncryptedSecrets.get(0).getId());
        assertNewKeyId(reEncryptedSecrets.get(0).getSecretValue());
    }

}

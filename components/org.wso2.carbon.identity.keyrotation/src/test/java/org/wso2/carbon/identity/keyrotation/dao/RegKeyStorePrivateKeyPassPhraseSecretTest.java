package org.wso2.carbon.identity.keyrotation.dao;

import org.wso2.carbon.identity.keyrotation.model.RegistryProperty;
import org.wso2.carbon.identity.keyrotation.service.DBKeyRotator;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegKeyStorePrivateKeyPassPhraseSecretTest extends BaseSecretTest {

    @Override
    public void setDatabaseQueries() {

        this.createTableQuery = "CREATE TABLE IF NOT EXISTS REG_PROPERTY ( " +
                " REG_ID INTEGER NOT NULL AUTO_INCREMENT, " +
                " REG_NAME CHARACTER VARYING(100) NOT NULL, " +
                " REG_VALUE CHARACTER VARYING(1000), " +
                " REG_TENANT_ID INTEGER DEFAULT 0 NOT NULL, " +
                " CONSTRAINT PK_REG_PROPERTY PRIMARY KEY (REG_ID,REG_TENANT_ID) " +
                ");";
        this.insertDataQuery = "INSERT INTO REG_PROPERTY" +
                "(REG_ID, REG_NAME, REG_VALUE, REG_TENANT_ID)" +
                "VALUES(25, '" + KeyRotationConstants.PRIVATE_KEY_PASS + "', '"
                + encryptedSecrets.get("regKeyStorePrivateKeyPass") + "', 3), (26, 'current-theme','Default', 3);";
    }

    @Override
    public void setChunkSize() {

        keyRotationConfig.setChunkSize(2);
    }

    @Override
    public void performAssertions() throws KeyRotationException {

        DBKeyRotator.getInstance().reEncryptKeystorePrivatekeyPassData(keyRotationConfig);

        List<RegistryProperty> reEncryptedSecrets =
                RegistryDAO.getInstance()
                        .getRegPropertyDataChunks(0, keyRotationConfig, KeyRotationConstants.PRIVATE_KEY_PASS);
        // Assertions
        assertEquals(1, reEncryptedSecrets.size());
        assertEquals("25", reEncryptedSecrets.get(0).getRegId());
        assertEquals(KeyRotationConstants.PRIVATE_KEY_PASS, reEncryptedSecrets.get(0).getRegName());
        assertEquals("3", reEncryptedSecrets.get(0).getRegTenantId());
        assertNewKeyId(reEncryptedSecrets.get(0).getRegValue());
    }
}

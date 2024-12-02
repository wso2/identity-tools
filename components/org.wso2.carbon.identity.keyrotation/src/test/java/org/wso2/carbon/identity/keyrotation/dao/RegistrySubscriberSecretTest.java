package org.wso2.carbon.identity.keyrotation.dao;

import org.wso2.carbon.identity.keyrotation.model.RegistryProperty;
import org.wso2.carbon.identity.keyrotation.service.DBKeyRotator;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegistrySubscriberSecretTest extends BaseSecretTest {

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
                "VALUES(21, '" + KeyRotationConstants.SUBSCRIBER_PASSWORD + "', '"
                + encryptedSecrets.get("regSubscriberSecret") + "', -1234), (22, 'subscriberName','pre-order', -1234);";
    }

    @Override
    public void setChunkSize() {

        keyRotationConfig.setChunkSize(2);
    }

    @Override
    public void performAssertions() throws KeyRotationException {

        DBKeyRotator.getInstance().reEncryptSubscriberPasswordData(keyRotationConfig);

        List<RegistryProperty> reEncryptedSecrets = RegistryDAO.getInstance()
                .getRegPropertyDataChunks(0, keyRotationConfig, KeyRotationConstants.SUBSCRIBER_PASSWORD);
        // Assertions
        assertEquals(1, reEncryptedSecrets.size());
        assertEquals("21", reEncryptedSecrets.get(0).getRegId());
        assertEquals(KeyRotationConstants.SUBSCRIBER_PASSWORD, reEncryptedSecrets.get(0).getRegName());
        assertEquals("-1234", reEncryptedSecrets.get(0).getRegTenantId());
        assertNewKeyId(reEncryptedSecrets.get(0).getRegValue());
    }

}

package org.wso2.carbon.identity.keyrotation.dao;

import org.wso2.carbon.identity.keyrotation.model.XACMLSubscriberSecret;
import org.wso2.carbon.identity.keyrotation.service.DBKeyRotator;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XAMLSubscriberSecretTest extends BaseSecretTest {

    @Override
    public void setDatabaseQueries() {

        this.createTableQuery = "CREATE TABLE IDN_XACML_SUBSCRIBER_PROPERTY ( " +
                " PROPERTY_ID CHARACTER VARYING(255) NOT NULL, " +
                " PROPERTY_VALUE CHARACTER VARYING(2000) NOT NULL, " +
                " IS_SECRET BOOLEAN DEFAULT FALSE NOT NULL, " +
                " SUBSCRIBER_ID CHARACTER VARYING(255) NOT NULL, " +
                " TENANT_ID INTEGER NOT NULL, " +
                " CONSTRAINT CONSTRAINT_FD PRIMARY KEY (SUBSCRIBER_ID,TENANT_ID,PROPERTY_ID));";
        this.insertDataQuery = "INSERT INTO IDN_XACML_SUBSCRIBER_PROPERTY " +
                "(PROPERTY_ID, PROPERTY_VALUE, IS_SECRET, SUBSCRIBER_ID, TENANT_ID) " +
                "VALUES('" + KeyRotationConstants.SUBSCRIBER_PASSWORD + "', '" +
                encryptedSecrets.get("xacmlSubscriberSecret") + "', true, '123', -1234), " +
                "('subscriberUserName','abb','false','123','-1234'), " +
                "('subscriberSecretAY','xAY','true','123','-1234');";
    }

    @Override
    public void setChunkSize() {
        keyRotationConfig.setChunkSize(2);
    }

    @Override
    public void performAssertions() throws KeyRotationException {

        DBKeyRotator.getInstance().reEncryptXACMLSubscriberSecrets(keyRotationConfig);
        List<XACMLSubscriberSecret> reEncryptedSecrets =
                IdentityDAO.getInstance()
                        .getXACMLSecretChunks(0, keyRotationConfig, KeyRotationConstants.SUBSCRIBER_PASSWORD);
        // Assertions
        assertEquals(1, reEncryptedSecrets.size());

        assertEquals("123", reEncryptedSecrets.get(0).getSubscriberId());
        assertEquals("-1234", reEncryptedSecrets.get(0).getTenantId());
        assertEquals(KeyRotationConstants.SUBSCRIBER_PASSWORD, reEncryptedSecrets.get(0).getPropertyKey());
        assertNewKeyId(reEncryptedSecrets.get(0).getPropertyValue());
    }
}

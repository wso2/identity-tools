package org.wso2.carbon.identity.keyrotation.dao;

import org.wso2.carbon.identity.keyrotation.model.BPSPassword;
import org.wso2.carbon.identity.keyrotation.service.DBKeyRotator;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorkflowBPSProfileSecretTest extends BaseSecretTest {

    @Override
    public void setDatabaseQueries() {

        this.createTableQuery = "CREATE TABLE WF_BPS_PROFILE ( " +
                " PROFILE_NAME CHARACTER VARYING(45) NOT NULL, " +
                " PASSWORD CHARACTER VARYING(1023), " +
                " USERNAME CHARACTER VARYING(100)," +
                " TENANT_ID INTEGER DEFAULT -1 NOT NULL, " +
                " CONSTRAINT CONSTRAINT_46 PRIMARY KEY (TENANT_ID,PROFILE_NAME) " +
                ");";
        this.insertDataQuery = "INSERT INTO PUBLIC.WF_BPS_PROFILE " +
                "(PROFILE_NAME, USERNAME, PASSWORD, TENANT_ID) " +
                "VALUES('embeded_bps','admin' ,'" + encryptedSecrets.get("wfBPSProfileSecret") + "', -1234);";
    }

    @Override
    public void setChunkSize() {

        keyRotationConfig.setChunkSize(2);
    }

    @Override
    public void performAssertions() throws KeyRotationException {

        DBKeyRotator.getInstance().reEncryptBPSData(keyRotationConfig);

        List<BPSPassword> reEncryptedSecrets = BPSProfileDAO.getInstance()
                .getBpsPasswordChunks(0, keyRotationConfig);
        // Assertions
        assertEquals(1, reEncryptedSecrets.size());
        assertEquals("embeded_bps", reEncryptedSecrets.get(0).getProfileName());
        assertEquals("-1234", reEncryptedSecrets.get(0).getTenantId());
        assertNewKeyId(reEncryptedSecrets.get(0).getPassword());
    }
}

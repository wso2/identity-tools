package org.wso2.carbon.identity.keyrotation.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.wso2.carbon.identity.keyrotation.model.WorkFlowRequestSecret;
import org.wso2.carbon.identity.keyrotation.service.DBKeyRotator;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkflowRequestSecretTest extends BaseSecretTest {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowRequestSecretTest.class);

    @Override
    public void setDatabaseQueries() {

        this.createTableQuery = "CREATE TABLE WF_REQUEST (  " +
                "  UUID CHARACTER VARYING(45) NOT NULL,  " +
                "  CREATED_BY CHARACTER VARYING(255),  " +
                "  TENANT_ID INTEGER DEFAULT -1 NOT NULL,  " +
                "  OPERATION_TYPE CHARACTER VARYING(50),  " +
                "  CREATED_AT TIMESTAMP,  " +
                "  UPDATED_AT TIMESTAMP,  " +
                "  STATUS CHARACTER VARYING(30),  " +
                "  REQUEST BINARY LARGE OBJECT,  " +
                "  CONSTRAINT CONSTRAINT_88 PRIMARY KEY (UUID)  " +
                ");";
    }

    @Override
    public void setChunkSize() {

        keyRotationConfig.setChunkSize(2);
    }

    @Override
    public void performAssertions() throws KeyRotationException {

        insertData();
        DBKeyRotator.getInstance().reEncryptWFRequestData(keyRotationConfig);
        List<WorkFlowRequestSecret> reEncryptedSecrets = WorkFlowDAO.getInstance()
                .getWFRequestChunks(0, keyRotationConfig);
        // Assertions
        assertEquals(1, reEncryptedSecrets.size());
        assertEquals("2f888f09-b681-402a-88fd-14112c006fa9",
                reEncryptedSecrets.get(0).getWorkflowRequest().getUuid());
        assertEquals("2024-11-27 17:14:59.755", reEncryptedSecrets.get(0).getUpdatedTime());
        assertNull(reEncryptedSecrets.get(0).getNewRequestSecret());
        assertNewKeyId(reEncryptedSecrets.get(0).getRequestSecret());
    }

    public void insertData() {

        String insertSQL = "INSERT INTO WF_REQUEST " +
                "(\"UUID\", CREATED_BY, TENANT_ID, OPERATION_TYPE, CREATED_AT, UPDATED_AT, STATUS, REQUEST) " +
                "VALUES('2f888f09-b681-402a-88fd-14112c006fa9', 'admin', -1234, 'ADD_USER', " +
                "'2024-11-27 17:14:59.755', '2024-11-27 17:14:59.755', 'PENDING',?);";

        try (Connection connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            // Set the BLOB data
            preparedStatement.setBytes(1,
                    hexStringToByteArray("" + encryptedSecrets.get("wfRequestSecret")));
            int rowsInserted = preparedStatement.executeUpdate();
            assertTrue(rowsInserted > 0);
        } catch (SQLException e) {
            logger.error("Error while executing the data insert script", e);
            Assert.fail();
        }
    }

    public static byte[] hexStringToByteArray(String hex) {

        int length = hex.length();
        byte[] byteArray = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            byteArray[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }

        return byteArray;
    }

}

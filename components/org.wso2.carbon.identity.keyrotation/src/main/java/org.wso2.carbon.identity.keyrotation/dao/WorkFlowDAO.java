/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.keyrotation.dao;

import org.wso2.carbon.identity.keyrotation.config.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to reEncrypt the WorkFlow data in DB.
 */
public class WorkFlowDAO {

    private static final WorkFlowDAO instance = new WorkFlowDAO();

    public WorkFlowDAO() {

    }

    public static WorkFlowDAO getInstance() {

        return instance;
    }

    /**
     * To retrieve the list of data in WF_REQUEST as chunks.
     *
     * @param startIndex        The start index of the data chunk.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public List<WorkflowRequest> getWFRequestChunks(int startIndex, KeyRotationConfig keyRotationConfig) throws
            KeyRotationException {

        List<WorkflowRequest> wfRequestList = new ArrayList<>();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.GET_WF_REQUEST)) {
                preparedStatement.setInt(1, startIndex);
                preparedStatement.setInt(2, DBConstants.CHUNK_SIZE);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    byte[] requestBytes = resultSet.getBytes(DBConstants.REQUEST);
                    WorkflowRequest wfRequest = deserializeWFRequest(requestBytes);
                    wfRequestList.add(wfRequest);
                }
            } catch (SQLException | IOException | ClassNotFoundException e) {
                throw new KeyRotationException("Error while retrieving requests from WF_REQUEST.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to identity DB.", e);
        }
        return wfRequestList;
    }

    /**
     * To reEncrypt the requests in WF_REQUEST using the new key.
     *
     * @param updateWfRequestList The list containing records that should be re-encrypted.
     * @param keyRotationConfig   Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public void updateWFRequestChunks(List<WorkflowRequest> updateWfRequestList, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.UPDATE_WF_REQUEST)) {
                for (WorkflowRequest wfRequest : updateWfRequestList) {
                    preparedStatement.setBytes(1, serializeWFRequest(wfRequest));
                    preparedStatement.setString(2, wfRequest.getUuid());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
            } catch (SQLException | IOException e) {
                connection.rollback();
                throw new KeyRotationException("Error while updating requests from WF_REQUEST.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to identity DB.", e);
        }
    }

    /**
     * To convert the stored byte stream to a WorkFlowRequest object.
     *
     * @param serializedData The byte stream.
     * @return WorkFlowRequest object.
     * @throws IOException            Exception thrown if something unexpected happens during I/O operations.
     * @throws ClassNotFoundException Exception thrown if something unexpected happens when loading the class.
     */
    private WorkflowRequest deserializeWFRequest(byte[] serializedData) throws IOException,
            ClassNotFoundException {

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedData);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Object objectRead = objectInputStream.readObject();
        return (WorkflowRequest) objectRead;
    }

    /**
     * To convert the passed WorkFlowRequest object to a byte stream.
     *
     * @param wfRequest WorkFlowRequest object.
     * @return The byte stream.
     * @throws IOException Exception thrown if something unexpected happens during I/O operations.
     */
    private byte[] serializeWFRequest(WorkflowRequest wfRequest) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(wfRequest);
        objectOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }
}
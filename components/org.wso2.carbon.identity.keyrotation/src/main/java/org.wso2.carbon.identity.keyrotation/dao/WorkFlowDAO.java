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

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.wso2.carbon.identity.keyrotation.config.model.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.model.WorkFlowRequestSecret;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class holds implementations needed to re-encrypt the WorkFlow data in DB.
 */
public class WorkFlowDAO {

    private static final Logger log = Logger.getLogger(WorkFlowDAO.class);
    private static final WorkFlowDAO instance = new WorkFlowDAO();
    public static int updateCount = 0;
    public static int failedUpdateCount = 0;

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
     * @return List of the retrieved records from the table.
     * @throws KeyRotationException Exception thrown while retrieving data from WF_REQUEST.
     */
    public List<WorkFlowRequestSecret> getWFRequestChunks(int startIndex, KeyRotationConfig keyRotationConfig) throws
            KeyRotationException {

        List<WorkFlowRequestSecret> workFlowRequestSecrets = new ArrayList<>();
        String query = DBConstants.GET_WF_REQUEST;
        int firstIndex = startIndex;
        int secIndex = keyRotationConfig.getChunkSize();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            connection.setAutoCommit(false);
            if (connection.getMetaData().getDriverName().contains(DBConstants.POSTGRESQL)) {
                query = DBConstants.GET_WF_REQUEST_POSTGRE;
                firstIndex = keyRotationConfig.getChunkSize();
                secIndex = startIndex;
            } else if (connection.getMetaData().getDriverName().contains(DBConstants.MSSQL) ||
                    connection.getMetaData().getDriverName().contains(DBConstants.ORACLE)) {
                query = DBConstants.GET_WF_REQUEST_OTHER;
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, firstIndex);
                preparedStatement.setInt(2, secIndex);
                ResultSet resultSet = preparedStatement.executeQuery();
                connection.commit();
                while (resultSet.next()) {
                    WorkFlowRequestSecret workFlowRequestSecret =
                            new WorkFlowRequestSecret(resultSet.getBytes(DBConstants.REQUEST), resultSet.getString(
                                    DBConstants.UPDATED_AT));
                    workFlowRequestSecrets.add(workFlowRequestSecret);
                }
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error while retrieving requests from WF_REQUEST.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to the identity DB.", e);
        }
        return workFlowRequestSecrets;
    }

    /**
     * To reEncrypt the requests in WF_REQUEST using the new key.
     *
     * @param updateWfRequestSecrets The list containing records that should be re-encrypted.
     * @param keyRotationConfig      Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while updating data from WF_REQUEST.
     */
    public void updateWFRequestChunks(List<WorkFlowRequestSecret> updateWfRequestSecrets,
                                      KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.UPDATE_WF_REQUEST)) {
                for (WorkFlowRequestSecret workFlowRequestSecret : updateWfRequestSecrets) {
                    byte[] reEncryptedData = workFlowRequestSecret.serializedReEncryptedWorkFlow();
                    if (reEncryptedData != null) {
                        preparedStatement.setBytes(1, reEncryptedData);
                        preparedStatement.setString(
                                2, workFlowRequestSecret.getWorkflowRequest().getUuid());
                        preparedStatement.setString(3, workFlowRequestSecret.getUpdatedTime());
                        preparedStatement.addBatch();
                    } else {
                        log.error("Cannot update the request in WF_REQUEST of record with uuid: " +
                                workFlowRequestSecret.getWorkflowRequest().getUuid());
                    }
                }
                preparedStatement.executeBatch();
                connection.commit();
                updateCount += updateWfRequestSecrets.size();
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error while updating requests in WF_REQUEST, trying the chunk row by row again. "
                        , e);
                retryOnRequestUpdate(updateWfRequestSecrets, connection);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to the identity DB.", e);
        }
    }

    /**
     * To retry upon a failure in updating request chunk in WF_REQUEST.
     *
     * @param updateWfRequestSecrets The list containing records that should be re-encrypted.
     * @param connection             Connection with the new identity DB.
     * @throws KeyRotationException Exception thrown while accessing new identity DB data.
     */
    private void retryOnRequestUpdate(List<WorkFlowRequestSecret> updateWfRequestSecrets, Connection connection)
            throws KeyRotationException {

        WorkFlowRequestSecret faulty = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.UPDATE_WF_REQUEST)) {
            for (WorkFlowRequestSecret workFlowRequestSecret : updateWfRequestSecrets) {
                try {
                    byte[] reEncryptedData = workFlowRequestSecret.serializedReEncryptedWorkFlow();
                    if (reEncryptedData != null) {
                        faulty = workFlowRequestSecret;
                        preparedStatement.setBytes(1, reEncryptedData);
                        preparedStatement.setString(
                                2, workFlowRequestSecret.getWorkflowRequest().getUuid());
                        preparedStatement.setString(3, workFlowRequestSecret.getUpdatedTime());
                        preparedStatement.executeUpdate();
                        connection.commit();
                        updateCount++;
                    } else {
                        log.error("Cannot update the request in WF_REQUEST of record with uuid: " +
                                workFlowRequestSecret.getWorkflowRequest().getUuid());
                    }
                } catch (SQLException err) {
                    connection.rollback();
                    log.error("Error while updating requests in WF_REQUEST of record with uuid: " +
                            faulty.getWorkflowRequest().getUuid() + " ," + err);
                    failedUpdateCount++;
                }
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while accessing new identity DB.", e);
        }
    }

    public List<String> generateWorkflowRequestBackup(List<WorkFlowRequestSecret> workFlowRequestSecrets) {

        if (CollectionUtils.isEmpty(workFlowRequestSecrets)) {
            return null;
        }
        List<String> backupStrings = new ArrayList<>();
        StringBuilder stringBuilder;

        for (WorkFlowRequestSecret workFlowRequestSecret : workFlowRequestSecrets) {

            String hexString = IntStream.range(0, workFlowRequestSecret.getSerializedWorkFlowRequest().length)
                    .mapToObj(i -> String.format("%02X", workFlowRequestSecret.getSerializedWorkFlowRequest()[i]))
                    .collect(Collectors.joining(""));


            stringBuilder = new StringBuilder("UPDATE WF_REQUEST SET");
            stringBuilder.append(" REQUEST='").append(hexString)
                    .append("' WHERE")
                    .append(" UUID='").append(workFlowRequestSecret.getWorkflowRequest().getUuid())
                    .append("' AND UPDATED_AT='").append(workFlowRequestSecret.getUpdatedTime())
                    .append("';").append(System.lineSeparator());
            backupStrings.add(stringBuilder.toString());
        }
        return backupStrings;
    }
}

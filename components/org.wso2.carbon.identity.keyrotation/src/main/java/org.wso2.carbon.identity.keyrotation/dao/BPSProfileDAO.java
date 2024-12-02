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
import org.wso2.carbon.identity.keyrotation.model.BPSPassword;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class holds implementations needed to re-encrypt the BPS Profile data in DB.
 */
public class BPSProfileDAO {

    private static final Logger log = Logger.getLogger(BPSProfileDAO.class);
    private static final BPSProfileDAO instance = new BPSProfileDAO();
    public static int updateCount = 0;
    public static int failedUpdateCount = 0;

    public BPSProfileDAO() {

    }

    public static BPSProfileDAO getInstance() {

        return instance;
    }

    /**
     * To retrieve the list of data in WF_BPS_PROFILE as chunks.
     *
     * @param startIndex        The start index of the data chunk.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List of the retrieved records from the table.
     * @throws KeyRotationException Exception thrown while retrieving data from WF_BPS_PROFILE.
     */
    public List<BPSPassword> getBpsPasswordChunks(int startIndex, KeyRotationConfig keyRotationConfig) throws
            KeyRotationException {

        List<BPSPassword> bpsPasswordList = new ArrayList<>();
        String query = DBConstants.GET_BPS_PASSWORD;
        int firstIndex = startIndex;
        int secIndex = keyRotationConfig.getChunkSize();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            connection.setAutoCommit(false);
            if (connection.getMetaData().getDriverName().contains(DBConstants.POSTGRESQL)) {
                query = DBConstants.GET_BPS_PASSWORD_POSTGRE;
                firstIndex = keyRotationConfig.getChunkSize();
                secIndex = startIndex;
            } else if (connection.getMetaData().getDriverName().contains(DBConstants.MSSQL) ||
                    connection.getMetaData().getDriverName().contains(DBConstants.ORACLE)) {
                query = DBConstants.GET_BPS_PASSWORD_OTHER;
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, firstIndex);
                preparedStatement.setInt(2, secIndex);
                ResultSet resultSet = preparedStatement.executeQuery();
                connection.commit();
                while (resultSet.next()) {
                    bpsPasswordList.add(new BPSPassword(resultSet.getString(KeyRotationConstants.PROFILE_NAME),
                            resultSet.getString(KeyRotationConstants.USERNAME),
                            resultSet.getString(KeyRotationConstants.TENANT_ID),
                            resultSet.getString(KeyRotationConstants.PASSWORD)));
                }
            } catch (SQLException e) {
                connection.rollback();
                throw new KeyRotationException("Error while retrieving passwords from WF_BPS_PROFILE.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to the identity DB.", e);
        }
        return bpsPasswordList;
    }

    /**
     * To reEncrypt the BPS passwords in WF_BPS_PROFILE using the new key.
     *
     * @param updateBPSPasswordsList The list containing records that should be re-encrypted.
     * @param keyRotationConfig      Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while updating data from WF_BPS_PROFILE.
     */
    public void updateBpsPasswordChunks(List<BPSPassword> updateBPSPasswordsList, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.UPDATE_BPS_PASSWORD)) {
                for (BPSPassword bpsPassword : updateBPSPasswordsList) {
                    preparedStatement.setString(1, bpsPassword.getNewPassword());
                    preparedStatement.setString(2, bpsPassword.getProfileName());
                    preparedStatement.setInt(3, Integer.parseInt(bpsPassword.getTenantId()));
                    preparedStatement.setString(4, bpsPassword.getPassword());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
                updateCount += updateBPSPasswordsList.size();
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error while updating passwords in WF_BPS_PROFILE, trying the chunk row by row " +
                        "again. ", e);
                retryOnBpsUpdate(updateBPSPasswordsList, connection);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to the identity DB.", e);
        }
    }

    /**
     * To retry upon a failure in updating the BPS chunk in WF_BPS_PROFILE.
     *
     * @param updateBPSPasswordsList The list containing records that should be re-encrypted.
     * @param connection             Connection with the new identity DB.
     * @throws KeyRotationException Exception thrown while accessing new identity DB data.
     */
    private void retryOnBpsUpdate(List<BPSPassword> updateBPSPasswordsList, Connection connection)
            throws KeyRotationException {

        BPSPassword faulty = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.UPDATE_BPS_PASSWORD)) {
            for (BPSPassword bpsPassword : updateBPSPasswordsList) {
                try {
                    faulty = bpsPassword;
                    preparedStatement.setString(1, bpsPassword.getPassword());
                    preparedStatement.setString(2, bpsPassword.getProfileName());
                    preparedStatement.setInt(3, Integer.parseInt(bpsPassword.getTenantId()));
                    preparedStatement.setString(4, bpsPassword.getPassword());
                    preparedStatement.executeUpdate();
                    connection.commit();
                    updateCount++;
                } catch (SQLException err) {
                    connection.rollback();
                    log.error("Error while updating password in WF_BPS_PROFILE of record with profile name: " +
                            faulty.getProfileName() + " , tenant id: " + faulty.getTenantId() + " ," + err);
                    failedUpdateCount++;
                }
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while accessing new identity DB.", e);
        }
    }

    public List<String> generateWorkflowBPSProfileBackup(List<BPSPassword> bpsPasswords) {

        if (CollectionUtils.isEmpty(bpsPasswords)) {
            return null;
        }
        List<String> backupStrings = new ArrayList<>();
        StringBuilder stringBuilder;

        for (BPSPassword bpsPassword : bpsPasswords) {
            stringBuilder = new StringBuilder("UPDATE WF_BPS_PROFILE SET");
            stringBuilder.append(" PASSWORD='").append(bpsPassword.getPassword())
                    .append("' WHERE")
                    .append(" PROFILE_NAME='").append(bpsPassword.getProfileName())
                    .append("' AND TENANT_ID='").append(bpsPassword.getTenantId())
                    .append("';").append(System.lineSeparator());
            backupStrings.add(stringBuilder.toString());
        }
        return backupStrings;
    }
}

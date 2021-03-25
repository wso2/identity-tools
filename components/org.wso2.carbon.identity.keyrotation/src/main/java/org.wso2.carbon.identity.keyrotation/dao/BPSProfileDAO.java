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
import org.wso2.carbon.identity.keyrotation.model.BPSPassword;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to reEncrypt the BPS Profile data in DB.
 */
public class BPSProfileDAO {

    private static final BPSProfileDAO instance = new BPSProfileDAO();
    private static final String PROFILE_NAME = "PROFILE_NAME";
    private static final String USERNAME = "USERNAME";
    private static final String TENANT_ID = "TENANT_ID";
    private static final String PASSWORD = "PASSWORD";

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
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public List<BPSPassword> getBpsPasswordChunks(int startIndex, KeyRotationConfig keyRotationConfig) throws
            KeyRotationException {

        List<BPSPassword> bpsPasswordList = new ArrayList<>();
        String query = DBConstants.GET_BPS_PASSWORD;
        int firstIndex = startIndex;
        int secIndex = DBConstants.CHUNK_SIZE;
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getNewIdnDBUrl(), keyRotationConfig.getNewIdnUsername(),
                        keyRotationConfig.getNewIdnPassword())) {
            if (connection.getMetaData().getDriverName().contains("PostgreSQL")) {
                query = DBConstants.GET_BPS_PASSWORD_POSTGRE;
                firstIndex = DBConstants.CHUNK_SIZE;
                secIndex = startIndex;
            } else if (connection.getMetaData().getDriverName().contains("SQL Server") ||
                    connection.getMetaData().getDriverName().contains("Oracle")) {
                query = DBConstants.GET_BPS_PASSWORD_OTHER;
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, firstIndex);
                preparedStatement.setInt(2, secIndex);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    bpsPasswordList.add(new BPSPassword(resultSet.getString(PROFILE_NAME),
                            resultSet.getString(USERNAME),
                            resultSet.getString(TENANT_ID),
                            resultSet.getString(PASSWORD)));
                }
            } catch (SQLException e) {
                throw new KeyRotationException("Error while retrieving passwords from WF_BPS_PROFILE.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
        return bpsPasswordList;
    }

    /**
     * To reEncrypt the BPS passwords in WF_BPS_PROFILE using the new key.
     *
     * @param updateBPSPasswordsList The list containing records that should be re-encrypted.
     * @param keyRotationConfig      Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public void updateBpsPasswordChunks(List<BPSPassword> updateBPSPasswordsList, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getNewIdnDBUrl(), keyRotationConfig.getNewIdnUsername(),
                        keyRotationConfig.getNewIdnPassword())) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.UPDATE_BPS_PASSWORD)) {
                for (BPSPassword bpsPassword : updateBPSPasswordsList) {
                    preparedStatement.setString(1, bpsPassword.getPassword());
                    preparedStatement.setString(2, bpsPassword.getProfileName());
                    preparedStatement.setInt(3, Integer.parseInt(bpsPassword.getTenantId()));
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new KeyRotationException("Error while updating passwords from WF_BPS_PROFILE.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
    }
}

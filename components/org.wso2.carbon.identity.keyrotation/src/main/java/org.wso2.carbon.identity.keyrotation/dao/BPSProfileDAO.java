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

import static org.wso2.carbon.identity.keyrotation.dao.DBConstants.CHUNK_SIZE;
import static org.wso2.carbon.identity.keyrotation.dao.DBConstants.GET_BPS_PASSWORD;
import static org.wso2.carbon.identity.keyrotation.dao.DBConstants.UPDATE_BPS_PASSWORD;

/**
 * Class to reEncrypt the BPS Profile data in DB.
 */
public class BPSProfileDAO {

    private static final BPSProfileDAO instance = new BPSProfileDAO();

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
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(GET_BPS_PASSWORD)) {
                preparedStatement.setInt(1, startIndex);
                preparedStatement.setInt(2, CHUNK_SIZE);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    bpsPasswordList.add(new BPSPassword(resultSet.getString("PROFILE_NAME"),
                            resultSet.getString("USERNAME"),
                            resultSet.getString("TENANT_ID"),
                            resultSet.getString("PASSWORD")));
                }
            } catch (SQLException e) {
                throw new KeyRotationException("Error while retrieving passwords from WF_BPS_PROFILE.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to identity DB.", e);
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
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_BPS_PASSWORD)) {
                for (BPSPassword bpsPassword : updateBPSPasswordsList) {
                    preparedStatement.setString(1, bpsPassword.getPassword());
                    preparedStatement.setString(2, bpsPassword.getProfileName());
                    preparedStatement.setString(3, bpsPassword.getTenantId());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new KeyRotationException("Error while updating passwords from WF_BPS_PROFILE.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to identity DB.", e);
        }
    }
}

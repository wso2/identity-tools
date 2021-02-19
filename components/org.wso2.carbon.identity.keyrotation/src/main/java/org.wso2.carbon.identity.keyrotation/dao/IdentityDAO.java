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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.keyrotation.dao.DBConstants.CHUNK_SIZE;
import static org.wso2.carbon.identity.keyrotation.dao.DBConstants.GET_IDENTITY_SECRET;
import static org.wso2.carbon.identity.keyrotation.dao.DBConstants.UPDATE_IDENTITY_SECRET;

/**
 * Class to reEncrypt the TOTP data in DB.
 */
public class IdentityDAO {

    private static final IdentityDAO instance = new IdentityDAO();

    public IdentityDAO() {

    }

    public static IdentityDAO getInstance() {

        return instance;
    }

    /**
     * To retrieve the list of data in IDN_IDENTITY_USER_DATA as chunks.
     *
     * @param startIndex        The start index of the data chunk.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public List<IdentitySecret> getIdentitySecretsChunks(int startIndex, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        List<IdentitySecret> identitySecretList = new ArrayList<>();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getDbUrl(), keyRotationConfig.getUsername(),
                        keyRotationConfig.getPassword())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(GET_IDENTITY_SECRET)) {
                preparedStatement.setString(1, String.valueOf(startIndex));
                preparedStatement.setInt(2, CHUNK_SIZE);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    identitySecretList
                            .add(new IdentitySecret(resultSet.getString("TENANT_ID"),
                                    resultSet.getString("USER_NAME"),
                                    resultSet.getString("DATA_KEY"),
                                    resultSet.getString("DATA_VALUE")));
                }
            } catch (SQLException e) {
                throw new KeyRotationException("Error while retrieving identity secrets from IDN_IDENTITY_USER_DATA.",
                        e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while getting connection to DB.",
                    e);
        }

        return identitySecretList;
    }

    /**
     * To reEncrypt the secret key in IDN_IDENTITY_USER_DATA using the new key.
     *
     * @param updateIdentitySecretList The list containing records that should be re-encrypted.
     * @param keyRotationConfig        Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public void updateIdentitySecretsChunks(List<IdentitySecret> updateIdentitySecretList,
                                            KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getDbUrl(), keyRotationConfig.getUsername(),
                        keyRotationConfig.getPassword())) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_IDENTITY_SECRET)) {
                for (IdentitySecret identitySecret : updateIdentitySecretList) {
                    preparedStatement.setString(1, identitySecret.getDataValue());
                    preparedStatement.setString(2, identitySecret.getTenantId());
                    preparedStatement.setString(3, identitySecret.getUsername());
                    preparedStatement.setString(4, identitySecret.getDataKey());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new KeyRotationException("Error while updating identity secrets from IDN_IDENTITY_USER_DATA.",
                        e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while getting connection to DB.",
                    e);
        }

    }

}

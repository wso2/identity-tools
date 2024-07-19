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

import org.apache.log4j.Logger;
import org.wso2.carbon.identity.keyrotation.config.model.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.model.TOTPSecret;
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
 * This class holds implementations needed to re-encrypt the TOTP data in DB.
 */
public class IdentityDAO {

    private static final Logger log = Logger.getLogger(IdentityDAO.class);
    private static final IdentityDAO instance = new IdentityDAO();
    public static int updateCount = 0;
    public static int failedUpdateCount = 0;
    public static int insertCount = 0;
    public static int failedInsertCount = 0;

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
     * @throws KeyRotationException Exception thrown while retrieving data from IDN_IDENTITY_USER_DATA.
     */
    public List<TOTPSecret> getTOTPSecretsChunks(int startIndex, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        List<TOTPSecret> totpSecretList = new ArrayList<>();
        String query = DBConstants.GET_TOTP_SECRET;
        int firstIndex = startIndex;
        int secIndex = keyRotationConfig.getChunkSize();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            connection.setAutoCommit(false);
            if (connection.getMetaData().getDriverName().contains(DBConstants.POSTGRESQL)) {
                query = DBConstants.GET_TOTP_SECRET_POSTGRE;
                firstIndex = keyRotationConfig.getChunkSize();
                secIndex = startIndex;
            } else if (connection.getMetaData().getDriverName().contains(DBConstants.MSSQL) ||
                    connection.getMetaData().getDriverName().contains(DBConstants.ORACLE)) {
                query = DBConstants.GET_TOTP_SECRET_OTHER;
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, DBConstants.SECRET_KEY);
                preparedStatement.setString(2, DBConstants.VERIFIED_SECRET_KEY);
                preparedStatement.setInt(3, firstIndex);
                preparedStatement.setInt(4, secIndex);
                ResultSet resultSet = preparedStatement.executeQuery();
                connection.commit();
                while (resultSet.next()) {
                    totpSecretList
                            .add(new TOTPSecret(resultSet.getString(KeyRotationConstants.TENANT_ID),
                                    resultSet.getString(KeyRotationConstants.USER_NAME),
                                    resultSet.getString(KeyRotationConstants.DATA_KEY),
                                    resultSet.getString(KeyRotationConstants.DATA_VALUE)));
                }
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error while retrieving TOTP secrets from IDN_IDENTITY_USER_DATA.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
        return totpSecretList;
    }

    /**
     * To reEncrypt the TOTP secret key in IDN_IDENTITY_USER_DATA using the new key.
     *
     * @param updateTOTPSecretList The list containing records that should be re-encrypted.
     * @param keyRotationConfig    Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while updating data from IDN_IDENTITY_USER_DATA.
     */
    public void updateTOTPSecretsChunks(List<TOTPSecret> updateTOTPSecretList,
                                        KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.UPDATE_TOTP_SECRET)) {
                for (TOTPSecret totpSecret : updateTOTPSecretList) {
                    preparedStatement.setString(1, totpSecret.getDataValue());
                    preparedStatement.setInt(2, Integer.parseInt(totpSecret.getTenantId()));
                    preparedStatement.setString(3, totpSecret.getUsername());
                    preparedStatement.setString(4, totpSecret.getDataKey());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
                updateCount += updateTOTPSecretList.size();
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error while updating TOTP secrets in IDN_IDENTITY_USER_DATA, trying the chunk " +
                        "row by row again. ", e);
                retryOnTOTOUpdate(updateTOTPSecretList, connection);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
    }

    /**
     * To retry upon a failure in updating the TOTP chunk in IDN_IDENTITY_USER_DATA.
     *
     * @param updateTOTPSecretList The list containing records that should be re-encrypted.
     * @param connection           Connection with the new identity DB.
     * @throws KeyRotationException Exception thrown while accessing new identity DB data.
     */
    private void retryOnTOTOUpdate(List<TOTPSecret> updateTOTPSecretList, Connection connection)
            throws KeyRotationException {

        TOTPSecret faulty = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.UPDATE_TOTP_SECRET)) {
            for (TOTPSecret totpSecret : updateTOTPSecretList) {
                try {
                    faulty = totpSecret;
                    preparedStatement.setString(1, totpSecret.getDataValue());
                    preparedStatement.setInt(2, Integer.parseInt(totpSecret.getTenantId()));
                    preparedStatement.setString(3, totpSecret.getUsername());
                    preparedStatement.setString(4, totpSecret.getDataKey());
                    preparedStatement.executeUpdate();
                    connection.commit();
                    updateCount++;
                } catch (SQLException err) {
                    connection.rollback();
                    log.error("Error while updating TOTP secret in IDN_IDENTITY_USER_DATA of record with tenant" +
                            " id: " + faulty.getTenantId() + " username: " + faulty.getUsername() + " data key: " +
                            faulty.getDataKey() + " ," + err);
                    failedUpdateCount++;
                }
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while accessing new identity DB.", e);
        }
    }
}

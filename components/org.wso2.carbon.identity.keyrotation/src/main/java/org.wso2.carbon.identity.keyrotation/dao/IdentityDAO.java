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

import org.wso2.carbon.identity.keyrotation.config.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.model.TOTPSecret;
import org.wso2.carbon.identity.keyrotation.model.TempTOTPSecret;
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
    private static final String TENANT_ID = "TENANT_ID";
    private static final String USER_NAME = "USER_NAME";
    private static final String DATA_KEY = "DATA_KEY";
    private static final String DATA_VALUE = "DATA_VALUE";
    private static final String AVAILABILITY = "AVAILABILITY";
    private static final String SYNC_ID = "SYNC_ID";
    private static final String SYNCED = "SYNCED";
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
        int secIndex = DBConstants.CHUNK_SIZE;
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getNewIdnDBUrl(), keyRotationConfig.getNewIdnUsername(),
                        keyRotationConfig.getNewIdnPassword())) {
            if (connection.getMetaData().getDriverName().contains(DBConstants.POSTGRESQL)) {
                query = DBConstants.GET_TOTP_SECRET_POSTGRE;
                firstIndex = DBConstants.CHUNK_SIZE;
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
                while (resultSet.next()) {
                    totpSecretList
                            .add(new TOTPSecret(resultSet.getString(TENANT_ID),
                                    resultSet.getString(USER_NAME),
                                    resultSet.getString(DATA_KEY),
                                    resultSet.getString(DATA_VALUE)));
                }
            } catch (SQLException e) {
                throw new KeyRotationException("Error while retrieving TOTP secrets from IDN_IDENTITY_USER_DATA.", e);
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
                .getConnection(keyRotationConfig.getNewIdnDBUrl(), keyRotationConfig.getNewIdnUsername(),
                        keyRotationConfig.getNewIdnPassword())) {
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

    /**
     * To retrieve the list of data in IDN_IDENTITY_USER_DATA_TEMP.
     *
     * @param syncId            The syncId of the data.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown while retrieving data from IDN_IDENTITY_USER_DATA_TEMP.
     */
    public List<TempTOTPSecret> getTempTOTPSecrets(int syncId, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        List<TempTOTPSecret> totpSecretList = new ArrayList<>();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getOldIdnDBUrl(), keyRotationConfig.getOldIdnUsername(),
                        keyRotationConfig.getOldIdnPassword())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.GET_TEMP_TOTP_SECRET)) {
                preparedStatement.setInt(1, syncId);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    totpSecretList
                            .add(new TempTOTPSecret(resultSet.getString(TENANT_ID),
                                    resultSet.getString(USER_NAME), resultSet.getString(DATA_KEY),
                                    resultSet.getString(DATA_VALUE), resultSet.getInt(AVAILABILITY),
                                    resultSet.getInt(SYNC_ID), resultSet.getInt(SYNCED)));
                }
            } catch (SQLException e) {
                throw new KeyRotationException("Error while retrieving TOTP secret from IDN_IDENTITY_USER_DATA_TEMP.",
                        e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to old identity DB.", e);
        }
        return totpSecretList;
    }

    /**
     * To retrieve the max sync id from similar primary key records in IDN_IDENTITY_USER_DATA_TEMP.
     *
     * @param record            A data record in IDN_IDENTITY_USER_DATA_TEMP table.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown while retrieving latest data from IDN_IDENTITY_USER_DATA_TEMP.
     */
    public List<TempTOTPSecret> getTempTOTPLatest(TempTOTPSecret record, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        List<TempTOTPSecret> totpSecretList = new ArrayList<>();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getOldIdnDBUrl(), keyRotationConfig.getOldIdnUsername(),
                        keyRotationConfig.getOldIdnPassword())) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(DBConstants.GET_TEMP_TOTP_SECRET_LATEST)) {
                preparedStatement.setInt(1, Integer.parseInt(record.getTenantId()));
                preparedStatement.setString(2, record.getUsername());
                preparedStatement.setString(3, record.getDataKey());
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    totpSecretList
                            .add(new TempTOTPSecret(resultSet.getString(TENANT_ID),
                                    resultSet.getString(USER_NAME), resultSet.getString(DATA_KEY),
                                    resultSet.getString(DATA_VALUE), resultSet.getInt(AVAILABILITY),
                                    resultSet.getInt(SYNC_ID), resultSet.getInt(SYNCED)));
                }
            } catch (SQLException e) {
                throw new KeyRotationException("Error while retrieving the latest TOTP secret from " +
                        "IDN_IDENTITY_USER_DATA_TEMP.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to old identity DB.", e);
        }
        return totpSecretList;
    }

    /**
     * To retrieve previous similar primary key records in IDN_IDENTITY_USER_DATA_TEMP.
     *
     * @param record            A data record in IDN_IDENTITY_USER_DATA_TEMP table.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown while retrieving previous data from IDN_IDENTITY_USER_DATA_TEMP.
     */
    public List<TempTOTPSecret> getTempTOTPPrevious(TempTOTPSecret record, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        List<TempTOTPSecret> totpSecretList = new ArrayList<>();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getOldIdnDBUrl(), keyRotationConfig.getOldIdnUsername(),
                        keyRotationConfig.getOldIdnPassword())) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(DBConstants.GET_TEMP_TOTP_SECRET_PREVIOUS)) {
                preparedStatement.setInt(1, Integer.parseInt(record.getTenantId()));
                preparedStatement.setString(2, record.getUsername());
                preparedStatement.setString(3, record.getDataKey());
                preparedStatement.setInt(4, record.getSyncId());
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    totpSecretList
                            .add(new TempTOTPSecret(resultSet.getString(TENANT_ID),
                                    resultSet.getString(USER_NAME), resultSet.getString(DATA_KEY),
                                    resultSet.getString(DATA_VALUE), resultSet.getInt(AVAILABILITY),
                                    resultSet.getInt(SYNC_ID), resultSet.getInt(SYNCED)));
                }
            } catch (SQLException e) {
                throw new KeyRotationException("Error while retrieving the previous TOTP secrets from " +
                        "IDN_IDENTITY_USER_DATA_TEMP.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to old identity DB.", e);
        }
        return totpSecretList;
    }

    /**
     * To update previous similar primary key records in IDN_IDENTITY_USER_DATA_TEMP.
     *
     * @param updateTOTPSecretList The list containing records that should be updated.
     * @param keyRotationConfig    Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while updating data in IDN_IDENTITY_USER_DATA_TEMP.
     */
    public void updateTOTPPreviousSimilarRecords(List<TempTOTPSecret> updateTOTPSecretList,
                                                 KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getOldIdnDBUrl(), keyRotationConfig.getOldIdnUsername(),
                        keyRotationConfig.getOldIdnPassword())) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(DBConstants.UPDATE_TEMP_TOTP_SECRET)) {
                for (TempTOTPSecret totpSecret : updateTOTPSecretList) {
                    preparedStatement.setInt(1, totpSecret.getSynced());
                    preparedStatement.setInt(2, totpSecret.getSyncId());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error while updating synced in IDN_IDENTITY_USER_DATA_TEMP", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to old identity DB.", e);
        }
    }

    /**
     * To insert the synced TOTP secret data into IDN_IDENTITY_USER_DATA.
     *
     * @param insertTOTPSecret  The record that should be inserted.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while inserting the data into IDN_IDENTITY_USER_DATA.
     */
    public void insertTOTPSecret(TempTOTPSecret insertTOTPSecret,
                                 KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getNewIdnDBUrl(), keyRotationConfig.getNewIdnUsername(),
                        keyRotationConfig.getNewIdnPassword())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.INSERT_TOTP_SECRET)) {
                preparedStatement.setInt(1, Integer.parseInt(insertTOTPSecret.getTenantId()));
                preparedStatement.setString(2, insertTOTPSecret.getUsername());
                preparedStatement.setString(3, insertTOTPSecret.getDataKey());
                preparedStatement.setString(4, insertTOTPSecret.getDataValue());
                preparedStatement.setString(5, insertTOTPSecret.getDataValue());
                preparedStatement.executeUpdate();
                insertCount++;
            } catch (SQLException e) {
                connection.rollback();
                failedInsertCount++;
                log.error("Error while inserting TOTP secret into IDN_IDENTITY_USER_DATA. ", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
    }

    /**
     * To delete the TOTP secret data in IDN_IDENTITY_USER_DATA.
     *
     * @param deleteTOTPSecret  The record that should be deleted.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while deleting data in IDN_IDENTITY_USER_DATA.
     */
    public void deleteTOTPSecret(TempTOTPSecret deleteTOTPSecret, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getNewIdnDBUrl(), keyRotationConfig.getNewIdnUsername(),
                        keyRotationConfig.getNewIdnPassword())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.DELETE_TOTP_SECRET)) {
                preparedStatement.setInt(1, Integer.parseInt(deleteTOTPSecret.getTenantId()));
                preparedStatement.setString(2, deleteTOTPSecret.getUsername());
                preparedStatement.setString(3, deleteTOTPSecret.getDataKey());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                log.error("Error while deleting TOTP secret in IDN_IDENTITY_USER_DATA. ", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
    }
}

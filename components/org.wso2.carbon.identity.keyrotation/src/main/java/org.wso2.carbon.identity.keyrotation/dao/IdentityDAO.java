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
import org.wso2.carbon.identity.keyrotation.model.IdentitySecret;
import org.wso2.carbon.identity.keyrotation.model.TOTPSecret;
import org.wso2.carbon.identity.keyrotation.model.XACMLSubscriberSecret;
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
    public static int updateIdentitySecretCount = 0;
    public static int failedUpdateIdentitySecretCount = 0;
    public static int updateXACMLSecretCount = 0;
    public static int failedXACMLSecretCount = 0;

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
     * @return List of the retrieved records from the table.
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
            throw new KeyRotationException("Error while connecting to the identity DB.", e);
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
                    preparedStatement.setString(1, totpSecret.getNewDataValue());
                    preparedStatement.setInt(2, Integer.parseInt(totpSecret.getTenantId()));
                    preparedStatement.setString(3, totpSecret.getUsername());
                    preparedStatement.setString(4, totpSecret.getDataKey());
                    preparedStatement.setString(5, totpSecret.getDataValue());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
                updateCount += updateTOTPSecretList.size();
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error while updating TOTP secrets in IDN_IDENTITY_USER_DATA, trying the chunk " +
                        "row by row again. ", e);
                retryOnTOTPUpdate(updateTOTPSecretList, connection);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to the identity DB.", e);
        }
    }

    /**
     * To retry upon a failure in updating the TOTP chunk in IDN_IDENTITY_USER_DATA.
     *
     * @param updateTOTPSecretList The list containing records that should be re-encrypted.
     * @param connection           Connection with the new identity DB.
     * @throws KeyRotationException Exception thrown while accessing new identity DB data.
     */
    private void retryOnTOTPUpdate(List<TOTPSecret> updateTOTPSecretList, Connection connection)
            throws KeyRotationException {

        TOTPSecret faulty = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.UPDATE_TOTP_SECRET)) {
            for (TOTPSecret totpSecret : updateTOTPSecretList) {
                try {
                    faulty = totpSecret;
                    preparedStatement.setString(1, totpSecret.getNewDataValue());
                    preparedStatement.setInt(2, Integer.parseInt(totpSecret.getTenantId()));
                    preparedStatement.setString(3, totpSecret.getUsername());
                    preparedStatement.setString(4, totpSecret.getDataKey());
                    preparedStatement.setString(5, totpSecret.getDataValue());
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
            throw new KeyRotationException(" Error while accessing the identity DB.", e);
        }
    }

    /**
     * To retrieve the list of data in IDN_SECRET as chunks.
     *
     * @param startIndex        The start index of the data chunk.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List of the retrieved records from the table.
     * @throws KeyRotationException Exception thrown while retrieving data from IDN_SECRET.
     */
    public List<IdentitySecret> getIdentitySecretChunks(int startIndex, KeyRotationConfig keyRotationConfig) throws
            KeyRotationException {

        List<IdentitySecret> identitySecretList = new ArrayList<>();
        String query = DBConstants.GET_IDN_SECRET;
        int firstIndex = startIndex;
        int secIndex = keyRotationConfig.getChunkSize();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            connection.setAutoCommit(false);
            if (connection.getMetaData().getDriverName().contains(DBConstants.POSTGRESQL)) {
                query = DBConstants.GET_IDN_SECRET_POSTGRE;
                firstIndex = keyRotationConfig.getChunkSize();
                secIndex = startIndex;
            } else if (connection.getMetaData().getDriverName().contains(DBConstants.MSSQL) ||
                    connection.getMetaData().getDriverName().contains(DBConstants.ORACLE)) {
                query = DBConstants.GET_IDN_SECRET_OTHER;
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, firstIndex);
                preparedStatement.setInt(2, secIndex);
                ResultSet resultSet = preparedStatement.executeQuery();
                connection.commit();
                while (resultSet.next()) {
                    identitySecretList.add(new IdentitySecret(resultSet.getString(KeyRotationConstants.ID),
                            resultSet.getString(KeyRotationConstants.SECRET_VALUE)));
                }
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error while retrieving secrets from IDN_SECRET.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to the identity DB.", e);
        }
        return identitySecretList;
    }

    /**
     * To reEncrypt the secrets in IDN_SECRET using the new key.
     *
     * @param updateIdentitySecretList The list containing records that should be updated in the DB.
     * @param keyRotationConfig        Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while updating data in IDN_SECRET.
     */
    public void updateIdentitySecretChunks(List<IdentitySecret> updateIdentitySecretList,
                                           KeyRotationConfig keyRotationConfig)
            throws
            KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.UPDATE_IDN_SECRET)) {
                for (IdentitySecret identitySecret : updateIdentitySecretList) {
                    preparedStatement.setString(1, identitySecret.getNewSecretValue());
                    preparedStatement.setString(2, identitySecret.getId());
                    preparedStatement.setString(3, identitySecret.getSecretValue());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
                updateIdentitySecretCount += updateIdentitySecretList.size();
            } catch (SQLException e) {
                connection.rollback();
                log.error(
                        "Error while updating Identity secrets in IDN_SECRET, trying the chunk " +
                                "row by row again. ", e);
                retryOnIdentitySecretUpdate(updateIdentitySecretList, connection);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to the identity DB.", e);
        }
    }

    /**
     * To retry upon a failure in updating Identity secret chunk in IDN_SECRET.
     *
     * @param updateIdentitySecretList The list containing records that should be re-encrypted.
     * @param connection               Connection with the new identity DB.
     * @throws KeyRotationException Exception thrown while accessing new identity DB data.
     */
    private void retryOnIdentitySecretUpdate(List<IdentitySecret> updateIdentitySecretList, Connection connection)
            throws KeyRotationException {

        IdentitySecret faulty = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.UPDATE_IDN_SECRET)) {
            for (IdentitySecret identitySecret : updateIdentitySecretList) {
                try {
                    faulty = identitySecret;
                    preparedStatement.setString(1, identitySecret.getNewSecretValue());
                    preparedStatement.setString(2, identitySecret.getId());
                    preparedStatement.setString(3, identitySecret.getSecretValue());
                    preparedStatement.executeUpdate();
                    connection.commit();
                    updateIdentitySecretCount++;
                } catch (SQLException err) {
                    connection.rollback();
                    log.error("Error while updating Identity secrets in IDN_SECRET of " +
                            "record with id: " + faulty.getId() + " ," + err);
                    failedUpdateIdentitySecretCount++;
                }
            }
        } catch (SQLException e) {
            throw new KeyRotationException(" Error while accessing the identity DB.", e);
        }
    }

    /**
     * To retrieve the list of data in IDN_XACML_SUBSCRIBER_PROPERTY as chunks.
     *
     * @param startIndex        The start index of the data chunk.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @param propertyKey       The property identifier key.
     * @return Retrieved list of data records
     * @throws KeyRotationException Exception thrown while retrieving data from IDN_SECRET.
     */
    public List<XACMLSubscriberSecret> getXACMLSecretChunks(int startIndex, KeyRotationConfig keyRotationConfig,
                                                            String propertyKey) throws
            KeyRotationException {

        List<XACMLSubscriberSecret> xacmlSubscriberSecrets = new ArrayList<>();
        String query = DBConstants.GET_XACML_SECRET;
        int firstIndex = startIndex;
        int secIndex = keyRotationConfig.getChunkSize();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            connection.setAutoCommit(false);
            if (connection.getMetaData().getDriverName().contains(DBConstants.POSTGRESQL)) {
                query = DBConstants.GET_XACML_SECRET_POSTGRE;
                firstIndex = keyRotationConfig.getChunkSize();
                secIndex = startIndex;
            } else if (connection.getMetaData().getDriverName().contains(DBConstants.MSSQL) ||
                    connection.getMetaData().getDriverName().contains(DBConstants.ORACLE)) {
                query = DBConstants.GET_XACML_SECRET_OTHER;
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, propertyKey);
                preparedStatement.setInt(2, firstIndex);
                preparedStatement.setInt(3, secIndex);
                ResultSet resultSet = preparedStatement.executeQuery();
                connection.commit();
                while (resultSet.next()) {
                    xacmlSubscriberSecrets.add(
                            new XACMLSubscriberSecret(resultSet.getString(KeyRotationConstants.SUBSCRIBER_ID),
                                    resultSet.getString(KeyRotationConstants.TENANT_ID),
                                    resultSet.getString(KeyRotationConstants.PROPERTY_ID),
                                    resultSet.getString(KeyRotationConstants.PROPERTY_VALUE)));
                }
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error while retrieving secrets from IDN_XACML_SUBSCRIBER_PROPERTY.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to the identity DB.", e);
        }
        return xacmlSubscriberSecrets;
    }

    /**
     * To reEncrypt the secrets in IDN_XACML_SUBSCRIBER_PROPERTY using the new key.
     *
     * @param xacmlSubscriberSecrets The list containing records that should be updated in the DB.
     * @param keyRotationConfig      Configuration data needed to perform the task.
     * @param propertyKey            The identifier of the XACML subscriber properties.
     * @throws KeyRotationException Exception thrown while updating data in IDN_SECRET.
     */
    public void updateXACMLSecretChunks(List<XACMLSubscriberSecret> xacmlSubscriberSecrets,
                                        KeyRotationConfig keyRotationConfig, String propertyKey)
            throws
            KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.UPDATE_XACML_SECRET)) {
                for (XACMLSubscriberSecret xacmlSubscriberSecret : xacmlSubscriberSecrets) {
                    preparedStatement.setString(1, xacmlSubscriberSecret.getNewPropertyValue());
                    preparedStatement.setString(2, xacmlSubscriberSecret.getPropertyKey());
                    preparedStatement.setString(3, xacmlSubscriberSecret.getSubscriberId());
                    preparedStatement.setString(4, xacmlSubscriberSecret.getTenantId());
                    preparedStatement.setString(5, xacmlSubscriberSecret.getPropertyValue());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
                updateXACMLSecretCount += xacmlSubscriberSecrets.size();
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error while updating xacml subscriber secrets in IDN_XACML_SUBSCRIBER_PROPERTY," +
                        " for the property: " + propertyKey + "trying the chunk row by row again. ", e);
                retryOnXACMLSecretUpdate(xacmlSubscriberSecrets, connection);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting the identity DB.", e);
        }
    }

    /**
     * To retry upon a failure in updating Identity secret chunk in IDN_XACML_SUBSCRIBER_PROPERTY.
     *
     * @param xacmlSubscriberSecrets The list containing records that should be re-encrypted.
     * @param connection             Connection with the new identity DB.
     * @throws KeyRotationException Exception thrown while accessing new identity DB data.
     */
    private void retryOnXACMLSecretUpdate(List<XACMLSubscriberSecret> xacmlSubscriberSecrets, Connection connection)
            throws KeyRotationException {

        XACMLSubscriberSecret faulty = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.UPDATE_XACML_SECRET)) {
            for (XACMLSubscriberSecret xacmlSubscriberSecret : xacmlSubscriberSecrets) {
                try {
                    faulty = xacmlSubscriberSecret;
                    preparedStatement.setString(1, xacmlSubscriberSecret.getNewPropertyValue());
                    preparedStatement.setString(2, xacmlSubscriberSecret.getPropertyKey());
                    preparedStatement.setString(3, xacmlSubscriberSecret.getSubscriberId());
                    preparedStatement.setString(4, xacmlSubscriberSecret.getTenantId());
                    preparedStatement.setString(5, xacmlSubscriberSecret.getPropertyValue());
                    preparedStatement.executeUpdate();
                    connection.commit();
                    updateXACMLSecretCount++;
                } catch (SQLException err) {
                    connection.rollback();
                    log.error("Error while updating the secrets in IDN_XACML_SUBSCRIBER_PROPERTY of " +
                            "record with tenant ID: " + faulty.getTenantId() + " , subscriber ID: " +
                            faulty.getSubscriberId() + ", secret ID: " + faulty.getPropertyKey() + err);
                    failedXACMLSecretCount++;
                }
            }
        } catch (SQLException e) {
            throw new KeyRotationException(" Error while accessing the identity DB.", e);
        }
    }

    public List<String> generateTOTPBackup(List<TOTPSecret> totpSecrets) {
        if (CollectionUtils.isEmpty(totpSecrets)) {
            return null;
        }
        List<String> backupStrings = new ArrayList<>();
        StringBuilder stringBuilder;

        for (TOTPSecret totpSecret : totpSecrets) {
            stringBuilder = new StringBuilder("UPDATE IDN_IDENTITY_USER_DATA SET");
            stringBuilder.append(" DATA_VALUE='").append(totpSecret.getDataValue())
                    .append("' WHERE")
                    .append(" USER_NAME='").append(totpSecret.getUsername())
                    .append("' AND TENANT_ID='").append(totpSecret.getTenantId())
                    .append("' AND DATA_KEY='").append(totpSecret.getDataKey())
                    .append("';").append(System.lineSeparator());
            backupStrings.add(stringBuilder.toString());
        }
        return backupStrings;
    }

    public List<String> generateIdentitySecretBackup(List<IdentitySecret> identitySecrets) {

        if (CollectionUtils.isEmpty(identitySecrets)) {
            return null;
        }
        List<String> backupStrings = new ArrayList<>();
        StringBuilder stringBuilder;

        for (IdentitySecret identitySecret : identitySecrets) {
            stringBuilder = new StringBuilder("UPDATE IDN_SECRET SET");
            stringBuilder.append(" SECRET_VALUE='").append(identitySecret.getSecretValue())
                    .append("' WHERE")
                    .append(" ID='").append(identitySecret.getId())
                    .append("';").append(System.lineSeparator());
            backupStrings.add(stringBuilder.toString());
        }
        return backupStrings;
    }

    public List<String> generateXACMLSubscriberBackup(List<XACMLSubscriberSecret> xacmlSubscriberSecrets) {

        if (CollectionUtils.isEmpty(xacmlSubscriberSecrets)) {
            return null;
        }
        List<String> backupStrings = new ArrayList<>();
        StringBuilder stringBuilder;

        for (XACMLSubscriberSecret xacmlSubscriberSecret : xacmlSubscriberSecrets) {
            stringBuilder = new StringBuilder("UPDATE IDN_XACML_SUBSCRIBER_PROPERTY SET");
            stringBuilder.append(" PROPERTY_VALUE='").append(xacmlSubscriberSecret.getPropertyValue())
                    .append("' WHERE")
                    .append(" PROPERTY_ID='").append(xacmlSubscriberSecret.getPropertyKey())
                    .append("' AND SUBSCRIBER_ID='").append(xacmlSubscriberSecret.getSubscriberId())
                    .append("' AND TENANT_ID='").append(xacmlSubscriberSecret.getTenantId())
                    .append("';").append(System.lineSeparator());
            backupStrings.add(stringBuilder.toString());
        }
        return backupStrings;
    }
}

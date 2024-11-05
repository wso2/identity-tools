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
import org.wso2.carbon.identity.keyrotation.model.OAuthCode;
import org.wso2.carbon.identity.keyrotation.model.OAuthSecret;
import org.wso2.carbon.identity.keyrotation.model.OAuthToken;
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
 * This class holds implementations needed to re-encrypt the OAuth data in DB.
 */
public class OAuthDAO {

    private static final Logger log = Logger.getLogger(OAuthDAO.class);
    private static final OAuthDAO instance = new OAuthDAO();
    public static int updateCodeCount = 0;
    public static int updateTokenCount = 0;
    public static int updateSecretCount = 0;
    public static int failedUpdateCodeCount = 0;
    public static int failedUpdateTokenCount = 0;
    public static int failedUpdateSecretCount = 0;

    public OAuthDAO() {

    }

    public static OAuthDAO getInstance() {

        return instance;
    }

    /**
     * To retrieve the list of data in IDN_OAUTH2_AUTHORIZATION_CODE as chunks.
     *
     * @param startIndex        The start index of the data chunk.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown while retrieving data from IDN_OAUTH2_AUTHORIZATION_CODE.
     */
    public List<OAuthCode> getOAuthCodeChunks(int startIndex, KeyRotationConfig keyRotationConfig) throws
            KeyRotationException {

        List<OAuthCode> oAuthCodeList = new ArrayList<>();
        String query = DBConstants.GET_OAUTH_AUTHORIZATION_CODE;
        int firstIndex = startIndex;
        int secIndex = keyRotationConfig.getChunkSize();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            connection.setAutoCommit(false);
            if (connection.getMetaData().getDriverName().contains(DBConstants.POSTGRESQL)) {
                query = DBConstants.GET_OAUTH_AUTHORIZATION_CODE_POSTGRE;
                firstIndex = keyRotationConfig.getChunkSize();
                secIndex = startIndex;
            } else if (connection.getMetaData().getDriverName().contains(DBConstants.MSSQL) ||
                    connection.getMetaData().getDriverName().contains(DBConstants.ORACLE)) {
                query = DBConstants.GET_OAUTH_AUTHORIZATION_CODE_OTHER;
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, firstIndex);
                preparedStatement.setInt(2, secIndex);
                ResultSet resultSet = preparedStatement.executeQuery();
                connection.commit();
                while (resultSet.next()) {
                    oAuthCodeList.add(new OAuthCode(resultSet.getString(KeyRotationConstants.CODE_ID),
                            resultSet.getString(KeyRotationConstants.AUTHORIZATION_CODE),
                            resultSet.getString(KeyRotationConstants.CONSUMER_KEY_ID)));
                }
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error while retrieving OAuth codes from IDN_OAUTH2_AUTHORIZATION_CODE.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
        return oAuthCodeList;
    }

    /**
     * To reEncrypt the OAuth code in IDN_OAUTH2_AUTHORIZATION_CODE using the new key.
     *
     * @param updateAuthCodeList The list containing records that should be updated in the DB.
     * @param keyRotationConfig  Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while updating data in IDN_OAUTH2_AUTHORIZATION_CODE.
     */
    public void updateOAuthCodeChunks(List<OAuthCode> updateAuthCodeList, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(DBConstants.UPDATE_OAUTH_AUTHORIZATION_CODE)) {
                for (OAuthCode oAuthCode : updateAuthCodeList) {
                    preparedStatement.setString(1, oAuthCode.getAuthorizationCode());
                    preparedStatement.setString(2, oAuthCode.getCodeId());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
                updateCodeCount += updateAuthCodeList.size();
            } catch (SQLException e) {
                connection.rollback();
                log.error(
                        "Error while updating OAuth codes in IDN_OAUTH2_AUTHORIZATION_CODE, trying the " +
                                "chunk row by row again. ", e);
                retryOnCodeUpdate(updateAuthCodeList, connection);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
    }

    /**
     * To retry upon a failure in updating OAuth code chunk in IDN_OAUTH2_AUTHORIZATION_CODE.
     *
     * @param updateAuthCodeList The list containing records that should be re-encrypted.
     * @param connection         Connection with the new identity DB.
     * @throws KeyRotationException Exception thrown while accessing new identity DB data.
     */
    private void retryOnCodeUpdate(List<OAuthCode> updateAuthCodeList, Connection connection)
            throws KeyRotationException {

        OAuthCode faulty = null;
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(DBConstants.UPDATE_OAUTH_AUTHORIZATION_CODE)) {
            for (OAuthCode oAuthCode : updateAuthCodeList) {
                try {
                    faulty = oAuthCode;
                    preparedStatement.setString(1, oAuthCode.getAuthorizationCode());
                    preparedStatement.setString(2, oAuthCode.getCodeId());
                    preparedStatement.executeUpdate();
                    connection.commit();
                    updateCodeCount++;
                } catch (SQLException err) {
                    connection.rollback();
                    log.error("Error while updating OAuth code in IDN_OAUTH2_AUTHORIZATION_CODE of record with " +
                            "code id: " + faulty.getCodeId() + " ," + err);
                    failedUpdateCodeCount++;
                }
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while accessing new identity DB.", e);
        }
    }

    /**
     * To retrieve the list of data in IDN_OAUTH2_ACCESS_TOKEN as chunks.
     *
     * @param startIndex        The start index of the data chunk.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown while retrieving data from IDN_OAUTH2_ACCESS_TOKEN.
     */
    public List<OAuthToken> getOAuthTokenChunks(int startIndex, KeyRotationConfig keyRotationConfig) throws
            KeyRotationException {

        List<OAuthToken> oAuthTokenList = new ArrayList<>();
        String query = DBConstants.GET_OAUTH_ACCESS_TOKEN;
        int firstIndex = startIndex;
        int secIndex = keyRotationConfig.getChunkSize();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            connection.setAutoCommit(false);
            if (connection.getMetaData().getDriverName().contains(DBConstants.POSTGRESQL)) {
                query = DBConstants.GET_OAUTH_ACCESS_TOKEN_POSTGRE;
                firstIndex = keyRotationConfig.getChunkSize();
                secIndex = startIndex;
            } else if (connection.getMetaData().getDriverName().contains(DBConstants.MSSQL) ||
                    connection.getMetaData().getDriverName().contains(DBConstants.ORACLE)) {
                query = DBConstants.GET_OAUTH_ACCESS_TOKEN_OTHER;
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, firstIndex);
                preparedStatement.setInt(2, secIndex);
                ResultSet resultSet = preparedStatement.executeQuery();
                connection.commit();
                while (resultSet.next()) {
                    oAuthTokenList.add(new OAuthToken(resultSet.getString(KeyRotationConstants.TOKEN_ID),
                            resultSet.getString(KeyRotationConstants.ACCESS_TOKEN),
                            resultSet.getString(KeyRotationConstants.REFRESH_TOKEN),
                            resultSet.getString(KeyRotationConstants.CONSUMER_KEY_ID)));
                }
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error while retrieving OAuth tokens from IDN_OAUTH2_ACCESS_TOKEN.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
        return oAuthTokenList;
    }

    /**
     * To reEncrypt the access and refresh tokens in IDN_OAUTH2_ACCESS_TOKEN using the new key.
     *
     * @param updateAuthTokensList The list containing records that should be updated in the DB.
     * @param keyRotationConfig    Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while updating data in IDN_OAUTH2_ACCESS_TOKEN.
     */
    public void updateOAuthTokenChunks(List<OAuthToken> updateAuthTokensList, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(DBConstants.UPDATE_OAUTH_ACCESS_TOKEN)) {
                for (OAuthToken oAuthToken : updateAuthTokensList) {
                    preparedStatement.setString(1, oAuthToken.getAccessToken());
                    preparedStatement.setString(2, oAuthToken.getRefreshToken());
                    preparedStatement.setString(3, oAuthToken.getTokenId());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
                updateTokenCount += updateAuthTokensList.size();
            } catch (SQLException e) {
                connection.rollback();
                log.error(
                        "Error while updating access and refresh tokens in IDN_OAUTH2_ACCESS_TOKEN, trying " +
                                "the chunk row by row again. ", e);
                retryOnTokenUpdate(updateAuthTokensList, connection);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
    }

    /**
     * To retry upon a failure in updating OAuth token chunk in IDN_OAUTH2_ACCESS_TOKEN.
     *
     * @param updateAuthTokensList The list containing records that should be re-encrypted.
     * @param connection           Connection with the new identity DB.
     * @throws KeyRotationException Exception thrown while accessing new identity DB data.
     */
    private void retryOnTokenUpdate(List<OAuthToken> updateAuthTokensList, Connection connection)
            throws KeyRotationException {

        OAuthToken faulty = null;
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(DBConstants.UPDATE_OAUTH_ACCESS_TOKEN)) {
            for (OAuthToken oAuthToken : updateAuthTokensList) {
                try {
                    faulty = oAuthToken;
                    preparedStatement.setString(1, oAuthToken.getAccessToken());
                    preparedStatement.setString(2, oAuthToken.getRefreshToken());
                    preparedStatement.setString(3, oAuthToken.getTokenId());
                    preparedStatement.executeUpdate();
                    connection.commit();
                    updateTokenCount++;
                } catch (SQLException err) {
                    connection.rollback();
                    log.error("Error while updating access and refresh tokens in IDN_OAUTH2_ACCESS_TOKEN of " +
                            "record with token id: " + faulty.getTokenId() + " ," + err);
                    failedUpdateTokenCount++;
                }
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while accessing new identity DB.", e);
        }
    }

    /**
     * To retrieve the list of data in IDN_OAUTH_CONSUMER_APPS as chunks.
     *
     * @param startIndex        The start index of the data chunk.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown while retrieving data from IDN_OAUTH_CONSUMER_APPS.
     */
    public List<OAuthSecret> getOAuthSecretChunks(int startIndex, KeyRotationConfig keyRotationConfig) throws
            KeyRotationException {

        List<OAuthSecret> oAuthSecretList = new ArrayList<>();
        String query = DBConstants.GET_OAUTH_SECRET;
        int firstIndex = startIndex;
        int secIndex = keyRotationConfig.getChunkSize();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            connection.setAutoCommit(false);
            if (connection.getMetaData().getDriverName().contains(DBConstants.POSTGRESQL)) {
                query = DBConstants.GET_OAUTH_SECRET_POSTGRE;
                firstIndex = keyRotationConfig.getChunkSize();
                secIndex = startIndex;
            } else if (connection.getMetaData().getDriverName().contains(DBConstants.MSSQL) ||
                    connection.getMetaData().getDriverName().contains(DBConstants.ORACLE)) {
                query = DBConstants.GET_OAUTH_SECRET_OTHER;
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, firstIndex);
                preparedStatement.setInt(2, secIndex);
                ResultSet resultSet = preparedStatement.executeQuery();
                connection.commit();
                while (resultSet.next()) {
                    oAuthSecretList.add(new OAuthSecret(resultSet.getString(KeyRotationConstants.ID),
                            resultSet.getString(KeyRotationConstants.CONSUMER_SECRET),
                            resultSet.getString(KeyRotationConstants.APP_NAME)));
                }
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error while retrieving secrets from IDN_OAUTH_CONSUMER_APPS.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
        return oAuthSecretList;
    }

    /**
     * To reEncrypt the secrets in IDN_OAUTH_CONSUMER_APPS using the new key.
     *
     * @param updateOAuthSecretList The list containing records that should be updated in the DB.
     * @param keyRotationConfig     Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while updating data in IDN_OAUTH_CONSUMER_APPS.
     */
    public void updateOAuthSecretChunks(List<OAuthSecret> updateOAuthSecretList, KeyRotationConfig keyRotationConfig)
            throws
            KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getIdnDBUrl(), keyRotationConfig.getIdnUsername(),
                        keyRotationConfig.getIdnPassword())) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.UPDATE_OAUTH_SECRET)) {
                for (OAuthSecret oAuthSecret : updateOAuthSecretList) {
                    preparedStatement.setString(1, oAuthSecret.getNewConsumerSecret());
                    preparedStatement.setInt(2, Integer.parseInt(oAuthSecret.getId()));
                    preparedStatement.setString(3, oAuthSecret.getConsumerSecret());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
                updateSecretCount += updateOAuthSecretList.size();
            } catch (SQLException e) {
                connection.rollback();
                log.error(
                        "Error while updating OAuth secrets in IDN_OAUTH_CONSUMER_APPS, trying the chunk " +
                                "row by row again. ", e);
                retryOnSecretUpdate(updateOAuthSecretList, connection);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
    }

    /**
     * To retry upon a failure in updating OAuth secret chunk in IDN_OAUTH_CONSUMER_APPS.
     *
     * @param updateOAuthSecretList The list containing records that should be re-encrypted.
     * @param connection            Connection with the new identity DB.
     * @throws KeyRotationException Exception thrown while accessing new identity DB data.
     */
    private void retryOnSecretUpdate(List<OAuthSecret> updateOAuthSecretList, Connection connection)
            throws KeyRotationException {

        OAuthSecret faulty = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.UPDATE_OAUTH_SECRET)) {
            for (OAuthSecret oAuthSecret : updateOAuthSecretList) {
                try {
                    faulty = oAuthSecret;
                    preparedStatement.setString(1, oAuthSecret.getNewConsumerSecret());
                    preparedStatement.setInt(2, Integer.parseInt(oAuthSecret.getId()));
                    preparedStatement.setString(3, oAuthSecret.getConsumerSecret());
                    preparedStatement.executeUpdate();
                    connection.commit();
                    updateSecretCount++;
                } catch (SQLException err) {
                    connection.rollback();
                    log.error("Error while updating OAuth secrets in IDN_OAUTH_CONSUMER_APPS of " +
                            "record with id: " + faulty.getId() + " ," + err);
                    failedUpdateSecretCount++;
                }
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while accessing new identity DB.", e);
        }
    }
}

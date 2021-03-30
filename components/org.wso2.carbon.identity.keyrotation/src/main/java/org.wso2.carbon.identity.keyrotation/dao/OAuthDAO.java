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
import org.wso2.carbon.identity.keyrotation.model.OAuthCode;
import org.wso2.carbon.identity.keyrotation.model.OAuthSecret;
import org.wso2.carbon.identity.keyrotation.model.OAuthToken;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to reEncrypt the OAuth data in DB.
 */
public class OAuthDAO {

    private static final Logger log = Logger.getLogger(OAuthDAO.class);
    private static final OAuthDAO instance = new OAuthDAO();
    private static final String CODE_ID = "CODE_ID";
    private static final String AUTHORIZATION_CODE = "AUTHORIZATION_CODE";
    private static final String CONSUMER_KEY_ID = "CONSUMER_KEY_ID";
    private static final String TOKEN_ID = "TOKEN_ID";
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String REFRESH_TOKEN = "REFRESH_TOKEN";
    private static final String ID = "ID";
    private static final String CONSUMER_SECRET = "CONSUMER_SECRET";
    private static final String APP_NAME = "APP_NAME";
    public static int updateCodeCount = 0;
    public static int updateTokenCount = 0;
    public static int updateSecretCount = 0;
    public static int failedCodeCount = 0;
    public static int failedTokenCount = 0;
    public static int failedSecretCount = 0;


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
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public List<OAuthCode> getOAuthCodeChunks(int startIndex, KeyRotationConfig keyRotationConfig) throws
            KeyRotationException {

        List<OAuthCode> oAuthCodeList = new ArrayList<>();
        String query = DBConstants.GET_OAUTH_AUTHORIZATION_CODE;
        int firstIndex = startIndex;
        int secIndex = DBConstants.CHUNK_SIZE;
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getNewIdnDBUrl(), keyRotationConfig.getNewIdnUsername(),
                        keyRotationConfig.getNewIdnPassword())) {
            if (connection.getMetaData().getDriverName().contains(DBConstants.POSTGRESQL)) {
                query = DBConstants.GET_OAUTH_AUTHORIZATION_CODE_POSTGRE;
                firstIndex = DBConstants.CHUNK_SIZE;
                secIndex = startIndex;
            } else if (connection.getMetaData().getDriverName().contains(DBConstants.MSSQL) ||
                    connection.getMetaData().getDriverName().contains(DBConstants.ORACLE)) {
                query = DBConstants.GET_OAUTH_AUTHORIZATION_CODE_OTHER;
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, firstIndex);
                preparedStatement.setInt(2, secIndex);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    oAuthCodeList.add(new OAuthCode(resultSet.getString(CODE_ID),
                            resultSet.getString(AUTHORIZATION_CODE),
                            resultSet.getString(CONSUMER_KEY_ID)));
                }
            } catch (SQLException e) {
                throw new KeyRotationException("Error while retrieving auth codes from IDN_OAUTH2_AUTHORIZATION_CODE.",
                        e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
        return oAuthCodeList;
    }

    /**
     * To reEncrypt the OAuth code in IDN_OAUTH2_AUTHORIZATION_CODE using the new key.
     *
     * @param updateAuthCodeList The list containing records that should be re-encrypted.
     * @param keyRotationConfig  Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public void updateOAuthCodeChunks(List<OAuthCode> updateAuthCodeList, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getNewIdnDBUrl(), keyRotationConfig.getNewIdnUsername(),
                        keyRotationConfig.getNewIdnPassword())) {
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
                OAuthCode faulty = updateAuthCodeList.get(0);
                log.error(
                        "Error while updating auth codes from IDN_OAUTH2_AUTHORIZATION_CODE, trying the " +
                                "chunk row by row again. ", e);
                PreparedStatement preparedStatement =
                        connection.prepareStatement(DBConstants.UPDATE_OAUTH_AUTHORIZATION_CODE);
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
                        log.error("Error while updating auth code from IDN_OAUTH2_AUTHORIZATION_CODE of record with " +
                                "code id: " + faulty.getCodeId() + " ," + err);
                        failedCodeCount++;
                    }
                }
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
    }

    /**
     * To retrieve the list of data in IDN_OAUTH2_ACCESS_TOKEN as chunks.
     *
     * @param startIndex        The start index of the data chunk.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public List<OAuthToken> getOAuthTokenChunks(int startIndex, KeyRotationConfig keyRotationConfig) throws
            KeyRotationException {

        List<OAuthToken> oAuthTokenList = new ArrayList<>();
        String query = DBConstants.GET_OAUTH_ACCESS_TOKEN;
        int firstIndex = startIndex;
        int secIndex = DBConstants.CHUNK_SIZE;
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getNewIdnDBUrl(), keyRotationConfig.getNewIdnUsername(),
                        keyRotationConfig.getNewIdnPassword())) {
            if (connection.getMetaData().getDriverName().contains(DBConstants.POSTGRESQL)) {
                query = DBConstants.GET_OAUTH_ACCESS_TOKEN_POSTGRE;
                firstIndex = DBConstants.CHUNK_SIZE;
                secIndex = startIndex;
            } else if (connection.getMetaData().getDriverName().contains(DBConstants.MSSQL) ||
                    connection.getMetaData().getDriverName().contains(DBConstants.ORACLE)) {
                query = DBConstants.GET_OAUTH_ACCESS_TOKEN_OTHER;
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, firstIndex);
                preparedStatement.setInt(2, secIndex);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    oAuthTokenList.add(new OAuthToken(resultSet.getString(TOKEN_ID),
                            resultSet.getString(ACCESS_TOKEN), resultSet.getString(REFRESH_TOKEN),
                            resultSet.getString(CONSUMER_KEY_ID)));
                }
            } catch (SQLException e) {
                throw new KeyRotationException("Error while retrieving auth tokens from IDN_OAUTH2_ACCESS_TOKEN.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
        return oAuthTokenList;
    }

    /**
     * To reEncrypt the access and refresh tokens in IDN_OAUTH2_ACCESS_TOKEN using the new key.
     *
     * @param updateAuthTokensList The list containing records that should be re-encrypted.
     * @param keyRotationConfig    Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public void updateOAuthTokenChunks(List<OAuthToken> updateAuthTokensList, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getNewIdnDBUrl(), keyRotationConfig.getNewIdnUsername(),
                        keyRotationConfig.getNewIdnPassword())) {
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
                OAuthToken faulty = updateAuthTokensList.get(0);
                log.error(
                        "Error while updating access and refresh tokens from IDN_OAUTH2_ACCESS_TOKEN, trying " +
                                "the chunk row by row again. ", e);
                PreparedStatement preparedStatement =
                        connection.prepareStatement(DBConstants.UPDATE_OAUTH_ACCESS_TOKEN);
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
                        log.error("Error while updating access and refresh tokens from IDN_OAUTH2_ACCESS_TOKEN of " +
                                "record with token id: " + faulty.getTokenId() + " ," + err);
                        failedTokenCount++;
                    }
                }
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
    }

    /**
     * To retrieve the list of data in IDN_OAUTH_CONSUMER_APPS as chunks.
     *
     * @param startIndex        The start index of the data chunk.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public List<OAuthSecret> getOAuthSecretChunks(int startIndex, KeyRotationConfig keyRotationConfig) throws
            KeyRotationException {

        List<OAuthSecret> oAuthSecretList = new ArrayList<>();
        String query = DBConstants.GET_OAUTH_SECRET;
        int firstIndex = startIndex;
        int secIndex = DBConstants.CHUNK_SIZE;
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getNewIdnDBUrl(), keyRotationConfig.getNewIdnUsername(),
                        keyRotationConfig.getNewIdnPassword())) {
            if (connection.getMetaData().getDriverName().contains(DBConstants.POSTGRESQL)) {
                query = DBConstants.GET_OAUTH_SECRET_POSTGRE;
                firstIndex = DBConstants.CHUNK_SIZE;
                secIndex = startIndex;
            } else if (connection.getMetaData().getDriverName().contains(DBConstants.MSSQL) ||
                    connection.getMetaData().getDriverName().contains(DBConstants.ORACLE)) {
                query = DBConstants.GET_OAUTH_SECRET_OTHER;
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, firstIndex);
                preparedStatement.setInt(2, secIndex);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    oAuthSecretList.add(new OAuthSecret(resultSet.getString(ID),
                            resultSet.getString(CONSUMER_SECRET),
                            resultSet.getString(APP_NAME)));
                }
            } catch (SQLException e) {
                throw new KeyRotationException("Error while retrieving secrets from IDN_OAUTH_CONSUMER_APPS.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
        return oAuthSecretList;
    }

    /**
     * To reEncrypt the secrets in IDN_OAUTH_CONSUMER_APPS using the new key.
     *
     * @param updateOAuthSecretList The list containing records that should be re-encrypted.
     * @param keyRotationConfig     Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public void updateOAuthSecretChunks(List<OAuthSecret> updateOAuthSecretList, KeyRotationConfig keyRotationConfig)
            throws
            KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getNewIdnDBUrl(), keyRotationConfig.getNewIdnUsername(),
                        keyRotationConfig.getNewIdnPassword())) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.UPDATE_OAUTH_SECRET)) {
                for (OAuthSecret oAuthSecret : updateOAuthSecretList) {
                    preparedStatement.setString(1, oAuthSecret.getConsumerSecret());
                    preparedStatement.setInt(2, Integer.parseInt(oAuthSecret.getId()));
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
                updateSecretCount += updateOAuthSecretList.size();
            } catch (SQLException e) {
                connection.rollback();
                OAuthSecret faulty = updateOAuthSecretList.get(0);
                log.error(
                        "Error while updating OAuth secrets from IDN_OAUTH_CONSUMER_APPS, trying the chunk " +
                                "row by row again. ", e);
                PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.UPDATE_OAUTH_SECRET);
                for (OAuthSecret oAuthSecret : updateOAuthSecretList) {
                    try {
                        faulty = oAuthSecret;
                        preparedStatement.setString(1, oAuthSecret.getConsumerSecret());
                        preparedStatement.setInt(2, Integer.parseInt(oAuthSecret.getId()));
                        preparedStatement.executeUpdate();
                        connection.commit();
                        updateSecretCount++;
                    } catch (SQLException err) {
                        connection.rollback();
                        log.error("Error while updating OAuth secrets from IDN_OAUTH_CONSUMER_APPS of " +
                                "record with id: " + faulty.getId() + " ," + err);
                        failedSecretCount++;
                    }
                }
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
    }
}

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
import org.wso2.carbon.identity.keyrotation.model.TempOAuthCode;
import org.wso2.carbon.identity.keyrotation.model.TempOAuthScope;
import org.wso2.carbon.identity.keyrotation.model.TempOAuthToken;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * This class holds implementations needed to re-encrypt the OAuth data in DB.
 */
public class OAuthDAO {

    private static final Logger log = Logger.getLogger(OAuthDAO.class);
    private static final OAuthDAO instance = new OAuthDAO();
    private static final String CODE_ID = "CODE_ID";
    private static final String AUTHORIZATION_CODE = "AUTHORIZATION_CODE";
    private static final String CONSUMER_KEY_ID = "CONSUMER_KEY_ID";
    private static final String CALLBACK_URL = "CALLBACK_URL";
    private static final String SCOPE = "SCOPE";
    private static final String AUTHZ_USER = "AUTHZ_USER";
    private static final String USER_DOMAIN = "USER_DOMAIN";
    private static final String TIME_CREATED = "TIME_CREATED";
    private static final String VALIDITY_PERIOD = "VALIDITY_PERIOD";
    private static final String STATE = "STATE";
    private static final String SUBJECT_IDENTIFIER = "SUBJECT_IDENTIFIER";
    private static final String PKCE_CODE_CHALLENGE = "PKCE_CODE_CHALLENGE";
    private static final String PKCE_CODE_CHALLENGE_METHOD = "PKCE_CODE_CHALLENGE_METHOD";
    private static final String AUTHORIZATION_CODE_HASH = "AUTHORIZATION_CODE_HASH";
    private static final String IDP_ID = "IDP_ID";
    private static final String TOKEN_ID = "TOKEN_ID";
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String REFRESH_TOKEN = "REFRESH_TOKEN";
    private static final String ID = "ID";
    private static final String CONSUMER_SECRET = "CONSUMER_SECRET";
    private static final String APP_NAME = "APP_NAME";
    private static final String TOKEN_SCOPE = "TOKEN_SCOPE";
    private static final String TENANT_ID = "TENANT_ID";
    private static final String USER_TYPE = "USER_TYPE";
    private static final String GRANT_TYPE = "GRANT_TYPE";
    private static final String REFRESH_TOKEN_TIME_CREATED = "REFRESH_TOKEN_TIME_CREATED";
    private static final String REFRESH_TOKEN_VALIDITY_PERIOD = "REFRESH_TOKEN_VALIDITY_PERIOD";
    private static final String TOKEN_SCOPE_HASH = "TOKEN_SCOPE_HASH";
    private static final String TOKEN_STATE = "TOKEN_STATE";
    private static final String TOKEN_STATE_ID = "TOKEN_STATE_ID";
    private static final String ACCESS_TOKEN_HASH = "ACCESS_TOKEN_HASH";
    private static final String REFRESH_TOKEN_HASH = "REFRESH_TOKEN_HASH";
    private static final String TOKEN_BINDING_REF = "TOKEN_BINDING_REF";
    private static final String AVAILABILITY = "AVAILABILITY";
    private static final String SYNC_ID = "SYNC_ID";
    private static final String SYNCED = "SYNCED";
    public static int updateCodeCount = 0;
    public static int updateTokenCount = 0;
    public static int updateSecretCount = 0;
    public static int insertCodeCount = 0;
    public static int insertTokenCount = 0;
    public static int insertScopeCount = 0;
    public static int failedUpdateCodeCount = 0;
    public static int failedUpdateTokenCount = 0;
    public static int failedUpdateSecretCount = 0;
    public static int failedInsertCodeCount = 0;
    public static int failedInsertTokenCount = 0;
    public static int failedInsertScopeCount = 0;

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
                            resultSet.getString(AUTHORIZATION_CODE), resultSet.getString(CONSUMER_KEY_ID)));
                }
            } catch (SQLException e) {
                throw new KeyRotationException("Error while retrieving OAuth codes from " +
                        "IDN_OAUTH2_AUTHORIZATION_CODE.", e);
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
                throw new KeyRotationException("Error while retrieving OAuth tokens from IDN_OAUTH2_ACCESS_TOKEN.", e);
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
     * @param updateOAuthSecretList The list containing records that should be updated in the DB.
     * @param keyRotationConfig     Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while updating data in IDN_OAUTH_CONSUMER_APPS.
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
                    preparedStatement.setString(1, oAuthSecret.getConsumerSecret());
                    preparedStatement.setInt(2, Integer.parseInt(oAuthSecret.getId()));
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

    /**
     * To retrieve the list of data in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP.
     *
     * @param syncId            The syncId of the data.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown while retrieving data from IDN_OAUTH2_AUTHORIZATION_CODE_TEMP.
     */
    public List<TempOAuthCode> getTempOAuthCode(int syncId, KeyRotationConfig keyRotationConfig) throws
            KeyRotationException {

        List<TempOAuthCode> oAuthCodeList = new ArrayList<>();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getOldIdnDBUrl(), keyRotationConfig.getOldIdnUsername(),
                        keyRotationConfig.getOldIdnPassword())) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(DBConstants.GET_TEMP_OAUTH_AUTHORIZATION_CODE)) {
                preparedStatement.setInt(1, syncId);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    oAuthCodeList.add(new TempOAuthCode(resultSet.getString(CODE_ID),
                            resultSet.getString(AUTHORIZATION_CODE), resultSet.getString(CONSUMER_KEY_ID),
                            resultSet.getString(CALLBACK_URL), resultSet.getString(SCOPE),
                            resultSet.getString(AUTHZ_USER), resultSet.getString(TENANT_ID),
                            resultSet.getString(USER_DOMAIN), resultSet.getString(TIME_CREATED),
                            resultSet.getString(VALIDITY_PERIOD), resultSet.getString(STATE),
                            resultSet.getString(TOKEN_ID), resultSet.getString(SUBJECT_IDENTIFIER),
                            resultSet.getString(PKCE_CODE_CHALLENGE), resultSet.getString(PKCE_CODE_CHALLENGE_METHOD),
                            resultSet.getString(AUTHORIZATION_CODE_HASH), resultSet.getString(IDP_ID),
                            resultSet.getInt(AVAILABILITY), resultSet.getInt(SYNC_ID), resultSet.getInt(SYNCED)));
                }
            } catch (SQLException e) {
                throw new KeyRotationException(
                        "Error while retrieving OAuth code from IDN_OAUTH2_AUTHORIZATION_CODE_TEMP.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to old identity DB.", e);
        }
        return oAuthCodeList;
    }

    /**
     * To retrieve the max sync id from similar primary key records in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP.
     *
     * @param record            A data record in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown while retrieving latest data from
     *                              IDN_OAUTH2_AUTHORIZATION_CODE_TEMP.
     */
    public List<TempOAuthCode> getTempOAuthCodeLatest(TempOAuthCode record, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        List<TempOAuthCode> oAuthCodeList = new ArrayList<>();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getOldIdnDBUrl(), keyRotationConfig.getOldIdnUsername(),
                        keyRotationConfig.getOldIdnPassword())) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(DBConstants.GET_TEMP_OAUTH_AUTHORIZATION_CODE_LATEST)) {
                preparedStatement.setString(1, record.getCodeId());
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    oAuthCodeList
                            .add(new TempOAuthCode(resultSet.getString(CODE_ID),
                                    resultSet.getString(AUTHORIZATION_CODE), resultSet.getString(CONSUMER_KEY_ID),
                                    resultSet.getString(CALLBACK_URL), resultSet.getString(SCOPE),
                                    resultSet.getString(AUTHZ_USER), resultSet.getString(TENANT_ID),
                                    resultSet.getString(USER_DOMAIN), resultSet.getString(TIME_CREATED),
                                    resultSet.getString(VALIDITY_PERIOD), resultSet.getString(STATE),
                                    resultSet.getString(TOKEN_ID), resultSet.getString(SUBJECT_IDENTIFIER),
                                    resultSet.getString(PKCE_CODE_CHALLENGE),
                                    resultSet.getString(PKCE_CODE_CHALLENGE_METHOD),
                                    resultSet.getString(AUTHORIZATION_CODE_HASH), resultSet.getString(IDP_ID),
                                    resultSet.getInt(AVAILABILITY), resultSet.getInt(SYNC_ID),
                                    resultSet.getInt(SYNCED)));
                }
            } catch (SQLException e) {
                throw new KeyRotationException("Error while retrieving the latest OAuth code from " +
                        "IDN_OAUTH2_AUTHORIZATION_CODE_TEMP.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to old identity DB.", e);
        }
        return oAuthCodeList;
    }

    /**
     * To retrieve previous similar primary key records in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP.
     *
     * @param record            A data record in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown while retrieving previous data from
     *                              IDN_OAUTH2_AUTHORIZATION_CODE_TEMP.
     */
    public List<TempOAuthCode> getTempOAuthCodePrevious(TempOAuthCode record, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        List<TempOAuthCode> oAuthCodeList = new ArrayList<>();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getOldIdnDBUrl(), keyRotationConfig.getOldIdnUsername(),
                        keyRotationConfig.getOldIdnPassword())) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(DBConstants.GET_TEMP_OAUTH_AUTHORIZATION_CODE_PREVIOUS)) {
                preparedStatement.setString(1, record.getCodeId());
                preparedStatement.setInt(2, record.getSyncId());
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    oAuthCodeList
                            .add(new TempOAuthCode(resultSet.getString(CODE_ID),
                                    resultSet.getString(AUTHORIZATION_CODE), resultSet.getString(CONSUMER_KEY_ID),
                                    resultSet.getString(CALLBACK_URL), resultSet.getString(SCOPE),
                                    resultSet.getString(AUTHZ_USER), resultSet.getString(TENANT_ID),
                                    resultSet.getString(USER_DOMAIN), resultSet.getString(TIME_CREATED),
                                    resultSet.getString(VALIDITY_PERIOD), resultSet.getString(STATE),
                                    resultSet.getString(TOKEN_ID), resultSet.getString(SUBJECT_IDENTIFIER),
                                    resultSet.getString(PKCE_CODE_CHALLENGE),
                                    resultSet.getString(PKCE_CODE_CHALLENGE_METHOD),
                                    resultSet.getString(AUTHORIZATION_CODE_HASH), resultSet.getString(IDP_ID),
                                    resultSet.getInt(AVAILABILITY), resultSet.getInt(SYNC_ID),
                                    resultSet.getInt(SYNCED)));
                }
            } catch (SQLException e) {
                throw new KeyRotationException("Error while retrieving the previous OAuth code from " +
                        "IDN_OAUTH2_AUTHORIZATION_CODE_TEMP.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to old identity DB.", e);
        }
        return oAuthCodeList;
    }

    /**
     * To update previous similar primary key records in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP.
     *
     * @param updateOAuthSecretList The list containing records that should be updated.
     * @param keyRotationConfig     Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while updating data in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP.
     */
    public void updateCodePreviousSimilarRecords(List<TempOAuthCode> updateOAuthSecretList,
                                                 KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getOldIdnDBUrl(), keyRotationConfig.getOldIdnUsername(),
                        keyRotationConfig.getOldIdnPassword())) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    DBConstants.UPDATE_TEMP_OAUTH_AUTHORIZATION_CODE)) {
                for (TempOAuthCode oAuthCode : updateOAuthSecretList) {
                    preparedStatement.setInt(1, oAuthCode.getSynced());
                    preparedStatement.setInt(2, oAuthCode.getSyncId());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error while updating synced in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to old identity DB.", e);
        }
    }

    /**
     * To insert the synced OAuth code into IDN_OAUTH2_AUTHORIZATION_CODE.
     *
     * @param insertAuthCode    The record that should be inserted.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while inserting data into IDN_OAUTH2_AUTHORIZATION_CODE.
     */
    public void insertOAuthCode(TempOAuthCode insertAuthCode, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getNewIdnDBUrl(), keyRotationConfig.getNewIdnUsername(),
                        keyRotationConfig.getNewIdnPassword())) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(DBConstants.INSERT_OAUTH_AUTHORIZATION_CODE)) {
                codeDuplicatedCode(preparedStatement, insertAuthCode);
                preparedStatement.executeUpdate();
                insertCodeCount++;
            } catch (SQLException e) {
                connection.rollback();
                failedInsertCodeCount++;
                log.error("Error while inserting OAuth codes into IDN_OAUTH2_AUTHORIZATION_CODE. ", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
    }

    /**
     * To delete the OAuth code data in IDN_OAUTH2_AUTHORIZATION_CODE.
     *
     * @param deleteAuthCode    The record that should be deleted.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while deleting data in IDN_OAUTH2_AUTHORIZATION_CODE.
     */
    public void deleteOAuthCode(TempOAuthCode deleteAuthCode, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getNewIdnDBUrl(), keyRotationConfig.getNewIdnUsername(),
                        keyRotationConfig.getNewIdnPassword())) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(DBConstants.DELETE_OAUTH_AUTHORIZATION_CODE)) {
                preparedStatement.setString(1, deleteAuthCode.getCodeId());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                log.error("Error while deleting OAuth codes in IDN_OAUTH2_AUTHORIZATION_CODE. ", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
    }

    /**
     * To retrieve the list of data in IDN_OAUTH2_ACCESS_TOKEN_TEMP.
     *
     * @param syncId            The syncId of the data.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown while retrieving data from IDN_OAUTH2_ACCESS_TOKEN_TEMP.
     */
    public List<TempOAuthToken> getTempOAuthToken(int syncId, KeyRotationConfig keyRotationConfig) throws
            KeyRotationException {

        List<TempOAuthToken> oAuthTokenList = new ArrayList<>();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getOldIdnDBUrl(), keyRotationConfig.getOldIdnUsername(),
                        keyRotationConfig.getOldIdnPassword())) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(DBConstants.GET_TEMP_OAUTH_ACCESS_TOKEN)) {
                preparedStatement.setInt(1, syncId);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    oAuthTokenList.add(new TempOAuthToken(resultSet.getString(TOKEN_ID),
                            resultSet.getString(ACCESS_TOKEN), resultSet.getString(REFRESH_TOKEN),
                            resultSet.getString(CONSUMER_KEY_ID), resultSet.getString(AUTHZ_USER),
                            resultSet.getString(TENANT_ID), resultSet.getString(USER_DOMAIN),
                            resultSet.getString(USER_TYPE), resultSet.getString(GRANT_TYPE),
                            resultSet.getString(TIME_CREATED), resultSet.getString(REFRESH_TOKEN_TIME_CREATED),
                            resultSet.getString(VALIDITY_PERIOD), resultSet.getString(REFRESH_TOKEN_VALIDITY_PERIOD),
                            resultSet.getString(TOKEN_SCOPE_HASH), resultSet.getString(TOKEN_STATE),
                            resultSet.getString(TOKEN_STATE_ID), resultSet.getString(SUBJECT_IDENTIFIER),
                            resultSet.getString(ACCESS_TOKEN_HASH), resultSet.getString(REFRESH_TOKEN_HASH),
                            resultSet.getString(IDP_ID), resultSet.getString(TOKEN_BINDING_REF),
                            resultSet.getInt(AVAILABILITY), resultSet.getInt(SYNC_ID), resultSet.getInt(SYNCED)));
                }
            } catch (SQLException e) {
                throw new KeyRotationException("Error while retrieving OAuth token from IDN_OAUTH2_ACCESS_TOKEN_TEMP.",
                        e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to old identity DB.", e);
        }
        return oAuthTokenList;
    }

    /**
     * To retrieve the max sync id from similar primary key records in IDN_OAUTH2_ACCESS_TOKEN_TEMP.
     *
     * @param record            A data record in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown while retrieving latest data from IDN_OAUTH2_ACCESS_TOKEN_TEMP.
     */
    public List<TempOAuthToken> getTempOAuthTokenLatest(TempOAuthToken record, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        List<TempOAuthToken> oAuthTokenList = new ArrayList<>();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getOldIdnDBUrl(), keyRotationConfig.getOldIdnUsername(),
                        keyRotationConfig.getOldIdnPassword())) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(DBConstants.GET_TEMP_OAUTH_ACCESS_TOKEN_LATEST)) {
                preparedStatement.setString(1, record.getTokenId());
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    oAuthTokenList
                            .add(new TempOAuthToken(resultSet.getString(TOKEN_ID),
                                    resultSet.getString(ACCESS_TOKEN), resultSet.getString(REFRESH_TOKEN),
                                    resultSet.getString(CONSUMER_KEY_ID), resultSet.getString(AUTHZ_USER),
                                    resultSet.getString(TENANT_ID), resultSet.getString(USER_DOMAIN),
                                    resultSet.getString(USER_TYPE), resultSet.getString(GRANT_TYPE),
                                    resultSet.getString(TIME_CREATED), resultSet.getString(REFRESH_TOKEN_TIME_CREATED),
                                    resultSet.getString(VALIDITY_PERIOD),
                                    resultSet.getString(REFRESH_TOKEN_VALIDITY_PERIOD),
                                    resultSet.getString(TOKEN_SCOPE_HASH), resultSet.getString(TOKEN_STATE),
                                    resultSet.getString(TOKEN_STATE_ID), resultSet.getString(SUBJECT_IDENTIFIER),
                                    resultSet.getString(ACCESS_TOKEN_HASH), resultSet.getString(REFRESH_TOKEN_HASH),
                                    resultSet.getString(IDP_ID), resultSet.getString(TOKEN_BINDING_REF),
                                    resultSet.getInt(AVAILABILITY), resultSet.getInt(SYNC_ID),
                                    resultSet.getInt(SYNCED)));
                }
            } catch (SQLException e) {
                throw new KeyRotationException("Error while retrieving the latest OAuth token from " +
                        "IDN_OAUTH2_ACCESS_TOKEN_TEMP.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to old identity DB.", e);
        }
        return oAuthTokenList;
    }

    /**
     * To retrieve previous similar primary key records in IDN_OAUTH2_ACCESS_TOKEN_TEMP.
     *
     * @param record            A data record in IDN_OAUTH2_ACCESS_TOKEN_TEMP table.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown while retrieving previous data from IDN_OAUTH2_ACCESS_TOKEN_TEMP.
     */
    public List<TempOAuthToken> getTempOAuthTokenPrevious(TempOAuthToken record, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        List<TempOAuthToken> oAuthTokenList = new ArrayList<>();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getOldIdnDBUrl(), keyRotationConfig.getOldIdnUsername(),
                        keyRotationConfig.getOldIdnPassword())) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(DBConstants.GET_TEMP_OAUTH_ACCESS_TOKEN_PREVIOUS)) {
                preparedStatement.setString(1, record.getTokenId());
                preparedStatement.setInt(2, record.getSyncId());
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    oAuthTokenList
                            .add(new TempOAuthToken(resultSet.getString(TOKEN_ID),
                                    resultSet.getString(ACCESS_TOKEN), resultSet.getString(REFRESH_TOKEN),
                                    resultSet.getString(CONSUMER_KEY_ID), resultSet.getString(AUTHZ_USER),
                                    resultSet.getString(TENANT_ID), resultSet.getString(USER_DOMAIN),
                                    resultSet.getString(USER_TYPE), resultSet.getString(GRANT_TYPE),
                                    resultSet.getString(TIME_CREATED), resultSet.getString(REFRESH_TOKEN_TIME_CREATED),
                                    resultSet.getString(VALIDITY_PERIOD),
                                    resultSet.getString(REFRESH_TOKEN_VALIDITY_PERIOD),
                                    resultSet.getString(TOKEN_SCOPE_HASH), resultSet.getString(TOKEN_STATE),
                                    resultSet.getString(TOKEN_STATE_ID), resultSet.getString(SUBJECT_IDENTIFIER),
                                    resultSet.getString(ACCESS_TOKEN_HASH), resultSet.getString(REFRESH_TOKEN_HASH),
                                    resultSet.getString(IDP_ID), resultSet.getString(TOKEN_BINDING_REF),
                                    resultSet.getInt(AVAILABILITY), resultSet.getInt(SYNC_ID),
                                    resultSet.getInt(SYNCED)));
                }
            } catch (SQLException e) {
                throw new KeyRotationException("Error while retrieving the previous OAuth token from " +
                        "IDN_OAUTH2_ACCESS_TOKEN_TEMP.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to old identity DB.", e);
        }
        return oAuthTokenList;
    }

    /**
     * To update previous similar primary key records in IDN_OAUTH2_ACCESS_TOKEN_TEMP.
     *
     * @param updateAuthTokensList The list containing records that should be updated.
     * @param keyRotationConfig    Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while updating data in IDN_OAUTH2_ACCESS_TOKEN_TEMP.
     */
    public void updateTokenPreviousSimilarRecords(List<TempOAuthToken> updateAuthTokensList,
                                                  KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getOldIdnDBUrl(), keyRotationConfig.getOldIdnUsername(),
                        keyRotationConfig.getOldIdnPassword())) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    DBConstants.UPDATE_TEMP_OAUTH_ACCESS_TOKEN)) {
                for (TempOAuthToken oAuthToken : updateAuthTokensList) {
                    preparedStatement.setInt(1, oAuthToken.getSynced());
                    preparedStatement.setInt(2, oAuthToken.getSyncId());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error while updating synced in IDN_OAUTH2_ACCESS_TOKEN_TEMP", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to old identity DB.", e);
        }
    }

    /**
     * To insert the synced access and refresh tokens into IDN_OAUTH2_ACCESS_TOKEN.
     *
     * @param insertAuthToken   The record that should be inserted.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while inserting data into IDN_OAUTH2_ACCESS_TOKEN.
     */
    public void insertOAuthTokens(TempOAuthToken insertAuthToken, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getNewIdnDBUrl(), keyRotationConfig.getNewIdnUsername(),
                        keyRotationConfig.getNewIdnPassword())) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(DBConstants.INSERT_OAUTH_ACCESS_TOKEN)) {
                tokenDuplicatedCode(preparedStatement, insertAuthToken);
                preparedStatement.executeUpdate();
                insertTokenCount++;
            } catch (SQLException e) {
                connection.rollback();
                failedInsertTokenCount++;
                log.error("Error while inserting access and refresh tokens into IDN_OAUTH2_ACCESS_TOKEN. ", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
    }

    /**
     * To delete the access and refresh token data in IDN_OAUTH2_ACCESS_TOKEN.
     *
     * @param deleteAuthToken   The record that should be deleted.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while deleting data in IDN_OAUTH2_ACCESS_TOKEN.
     */
    public void deleteOAuthToken(TempOAuthToken deleteAuthToken, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getNewIdnDBUrl(), keyRotationConfig.getNewIdnUsername(),
                        keyRotationConfig.getNewIdnPassword())) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(DBConstants.DELETE_OAUTH_ACCESS_TOKEN)) {
                preparedStatement.setString(1, deleteAuthToken.getTokenId());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                log.error("Error while deleting access and refresh token in IDN_OAUTH2_ACCESS_TOKEN. ", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
    }

    /**
     * To retrieve the list of data in IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP.
     *
     * @param syncId            The syncId of the data.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown while retrieving data from IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP.
     */
    public List<TempOAuthScope> getTempOAuthScope(int syncId, KeyRotationConfig keyRotationConfig) throws
            KeyRotationException {

        List<TempOAuthScope> tempOAuthScopeList = new ArrayList<>();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getOldIdnDBUrl(), keyRotationConfig.getOldIdnUsername(),
                        keyRotationConfig.getOldIdnPassword())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.GET_TEMP_OAUTH_SCOPE)) {
                preparedStatement.setInt(1, syncId);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    tempOAuthScopeList.add(new TempOAuthScope(resultSet.getString(TOKEN_ID),
                            resultSet.getString(TOKEN_SCOPE), resultSet.getString(TENANT_ID),
                            resultSet.getInt(AVAILABILITY), resultSet.getInt(SYNC_ID), resultSet.getInt(SYNCED)));
                }
            } catch (SQLException e) {
                throw new KeyRotationException(
                        "Error while retrieving OAuth scope from IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to old identity DB.", e);
        }
        return tempOAuthScopeList;
    }

    /**
     * To retrieve the max sync id from similar primary key records in IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP.
     *
     * @param record            A data record in IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP table.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown while retrieving latest data from
     *                              IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP.
     */
    public List<TempOAuthScope> getTempOAuthScopeLatest(TempOAuthScope record, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        List<TempOAuthScope> tempOAuthScopeList = new ArrayList<>();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getOldIdnDBUrl(), keyRotationConfig.getOldIdnUsername(),
                        keyRotationConfig.getOldIdnPassword())) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(DBConstants.GET_TEMP_OAUTH_SCOPE_LATEST)) {
                preparedStatement.setString(1, record.getTokenId());
                preparedStatement.setString(2, record.getTokenScope());
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    tempOAuthScopeList
                            .add(new TempOAuthScope(resultSet.getString(TOKEN_ID), resultSet.getString(TOKEN_SCOPE),
                                    resultSet.getString(TENANT_ID), resultSet.getInt(AVAILABILITY),
                                    resultSet.getInt(SYNC_ID), resultSet.getInt(SYNCED)));
                }
            } catch (SQLException e) {
                throw new KeyRotationException("Error while retrieving the latest OAuth scope from " +
                        "IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to old identity DB.", e);
        }
        return tempOAuthScopeList;
    }

    /**
     * To retrieve previous similar primary key records in IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP.
     *
     * @param record            A data record in IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP table.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown while retrieving previous data from
     *                              IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP.
     */
    public List<TempOAuthScope> getTempOAuthScopePrevious(TempOAuthScope record, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        List<TempOAuthScope> tempOAuthScopeList = new ArrayList<>();
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getOldIdnDBUrl(), keyRotationConfig.getOldIdnUsername(),
                        keyRotationConfig.getOldIdnPassword())) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(DBConstants.GET_TEMP_OAUTH_SCOPE_PREVIOUS)) {
                preparedStatement.setString(1, record.getTokenId());
                preparedStatement.setString(2, record.getTokenScope());
                preparedStatement.setInt(3, record.getSyncId());
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    tempOAuthScopeList
                            .add(new TempOAuthScope(resultSet.getString(TOKEN_ID), resultSet.getString(TOKEN_SCOPE),
                                    resultSet.getString(TENANT_ID), resultSet.getInt(AVAILABILITY),
                                    resultSet.getInt(SYNC_ID), resultSet.getInt(SYNCED)));
                }
            } catch (SQLException e) {
                throw new KeyRotationException("Error while retrieving the previous OAuth scope from " +
                        "IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to old identity DB.", e);
        }
        return tempOAuthScopeList;
    }

    /**
     * To update previous similar primary key records in IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP.
     *
     * @param updateAuthScopesList The list containing records that should be updated.
     * @param keyRotationConfig    Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while updating data in IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP.
     */
    public void updateScopePreviousSimilarRecords(List<TempOAuthScope> updateAuthScopesList,
                                                  KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getOldIdnDBUrl(), keyRotationConfig.getOldIdnUsername(),
                        keyRotationConfig.getOldIdnPassword())) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(DBConstants.UPDATE_TEMP_OAUTH_SCOPE)) {
                for (TempOAuthScope oAuthScope : updateAuthScopesList) {
                    preparedStatement.setInt(1, oAuthScope.getSynced());
                    preparedStatement.setInt(2, oAuthScope.getSyncId());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error while updating synced in IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to old identity DB.", e);
        }
    }

    /**
     * To insert the synced OAuth scope data into IDN_OAUTH2_ACCESS_TOKEN_SCOPE.
     *
     * @param insertAuthScope   The record that should be inserted.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while inserting data into IDN_OAUTH2_ACCESS_TOKEN_SCOPE.
     */
    public void insertOAuthScope(TempOAuthScope insertAuthScope, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getNewIdnDBUrl(), keyRotationConfig.getNewIdnUsername(),
                        keyRotationConfig.getNewIdnPassword())) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(DBConstants.INSERT_OAUTH_SCOPE)) {
                preparedStatement.setString(1, insertAuthScope.getTokenId());
                preparedStatement.setString(2, insertAuthScope.getTokenScope());
                preparedStatement.setInt(3, Integer.parseInt(insertAuthScope.getTenantId()));
                preparedStatement.setInt(4, Integer.parseInt(insertAuthScope.getTenantId()));
                preparedStatement.executeUpdate();
                insertScopeCount++;
            } catch (SQLException e) {
                connection.rollback();
                failedInsertScopeCount++;
                log.error("Error while inserting OAuth scope into IDN_OAUTH2_ACCESS_TOKEN_SCOPE. ", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
    }

    /**
     * To delete the OAuth scope data in IDN_OAUTH2_ACCESS_TOKEN_SCOPE.
     *
     * @param deleteAuthScope   The record that should be deleted.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while deleting data in IDN_OAUTH2_ACCESS_TOKEN_SCOPE.
     */
    public void deleteOAuthScope(TempOAuthScope deleteAuthScope, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getNewIdnDBUrl(), keyRotationConfig.getNewIdnUsername(),
                        keyRotationConfig.getNewIdnPassword())) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(DBConstants.DELETE_OAUTH_SCOPE)) {
                preparedStatement.setString(1, deleteAuthScope.getTokenId());
                preparedStatement.setString(2, deleteAuthScope.getTokenScope());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                log.error("Error while deleting OAuth scope in IDN_OAUTH2_ACCESS_TOKEN_SCOPE. ", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new identity DB.", e);
        }
    }

    /**
     * Method created to avoid duplication of the code in inserting OAuth codes.
     *
     * @param preparedStatement An object that represents a precompiled SQL statement.
     * @param oAuthCode         OAuthCode object.
     * @throws SQLException Exception thrown while accessing the database.
     */
    private void codeDuplicatedCode(PreparedStatement preparedStatement, TempOAuthCode oAuthCode) throws SQLException {

        preparedStatement.setString(1, oAuthCode.getCodeId());
        preparedStatement.setString(2, oAuthCode.getAuthorizationCode());
        preparedStatement.setInt(3, Integer.parseInt(oAuthCode.getConsumerKeyId()));
        preparedStatement.setString(4, oAuthCode.getCallbackUrl());
        preparedStatement.setString(5, oAuthCode.getScope());
        preparedStatement.setString(6, oAuthCode.getAuthzUser());
        preparedStatement.setInt(7, Integer.parseInt(oAuthCode.getTenantId()));
        preparedStatement.setString(8, oAuthCode.getUserDomain());
        preparedStatement.setTimestamp(9, Timestamp.valueOf(oAuthCode.getTimeCreated()));
        preparedStatement.setInt(10, Integer.parseInt(oAuthCode.getValidityPeriod()));
        preparedStatement.setString(11, oAuthCode.getState());
        preparedStatement.setString(12, oAuthCode.getTokenId());
        preparedStatement.setString(13, oAuthCode.getSubjectIdentifier());
        preparedStatement.setString(14, oAuthCode.getPkceCodeChallenge());
        preparedStatement.setString(15, oAuthCode.getPkceCodeChallengeMethod());
        preparedStatement.setString(16, oAuthCode.getAuthorizationCodeHash());
        preparedStatement.setInt(17, Integer.parseInt(oAuthCode.getIdpId()));
        preparedStatement.setString(18, oAuthCode.getAuthorizationCode());
        preparedStatement.setInt(19, Integer.parseInt(oAuthCode.getConsumerKeyId()));
        preparedStatement.setString(20, oAuthCode.getCallbackUrl());
        preparedStatement.setString(21, oAuthCode.getScope());
        preparedStatement.setString(22, oAuthCode.getAuthzUser());
        preparedStatement.setInt(23, Integer.parseInt(oAuthCode.getTenantId()));
        preparedStatement.setString(24, oAuthCode.getUserDomain());
        preparedStatement.setTimestamp(25, Timestamp.valueOf(oAuthCode.getTimeCreated()));
        preparedStatement.setInt(26, Integer.parseInt(oAuthCode.getValidityPeriod()));
        preparedStatement.setString(27, oAuthCode.getState());
        preparedStatement.setString(28, oAuthCode.getTokenId());
        preparedStatement.setString(29, oAuthCode.getSubjectIdentifier());
        preparedStatement.setString(30, oAuthCode.getPkceCodeChallenge());
        preparedStatement.setString(31, oAuthCode.getPkceCodeChallengeMethod());
        preparedStatement.setString(32, oAuthCode.getAuthorizationCodeHash());
        preparedStatement.setInt(33, Integer.parseInt(oAuthCode.getIdpId()));
    }

    /**
     * Method created to avoid duplication of the code in inserting OAuth tokens.
     *
     * @param preparedStatement An object that represents a precompiled SQL statement.
     * @param oAuthToken        OAuthToken object.
     * @throws SQLException Exception thrown while accessing the database.
     */
    private void tokenDuplicatedCode(PreparedStatement preparedStatement, TempOAuthToken oAuthToken)
            throws SQLException {

        preparedStatement.setString(1, oAuthToken.getTokenId());
        preparedStatement.setString(2, oAuthToken.getAccessToken());
        preparedStatement.setString(3, oAuthToken.getRefreshToken());
        preparedStatement.setInt(4, Integer.parseInt(oAuthToken.getConsumerKeyId()));
        preparedStatement.setString(5, oAuthToken.getAuthzUser());
        preparedStatement.setInt(6, Integer.parseInt(oAuthToken.getTenantId()));
        preparedStatement.setString(7, oAuthToken.getUserDomain());
        preparedStatement.setString(8, oAuthToken.getUserType());
        preparedStatement.setString(9, oAuthToken.getGrantType());
        preparedStatement.setTimestamp(10, Timestamp.valueOf(oAuthToken.getTimeCreated()));
        preparedStatement.setTimestamp(11,
                Timestamp.valueOf(oAuthToken.getRefreshTokenTimeCreated()));
        preparedStatement.setInt(12, Integer.parseInt(oAuthToken.getValidityPeriod()));
        preparedStatement.setInt(13,
                Integer.parseInt(oAuthToken.getRefreshTokenValidityPeriod()));
        preparedStatement.setString(14, oAuthToken.getTokenScopeHash());
        preparedStatement.setString(15, oAuthToken.getTokenState());
        preparedStatement.setString(16, oAuthToken.getTokenStateId());
        preparedStatement.setString(17, oAuthToken.getSubjectIdentifier());
        preparedStatement.setString(18, oAuthToken.getAccessTokenHash());
        preparedStatement.setString(19, oAuthToken.getRefreshTokenHash());
        preparedStatement.setInt(20, Integer.parseInt(oAuthToken.getIdpId()));
        preparedStatement.setString(21, oAuthToken.getTokenBindingRef());
        preparedStatement.setString(22, oAuthToken.getAccessToken());
        preparedStatement.setString(23, oAuthToken.getRefreshToken());
        preparedStatement.setInt(24, Integer.parseInt(oAuthToken.getConsumerKeyId()));
        preparedStatement.setString(25, oAuthToken.getAuthzUser());
        preparedStatement.setInt(26, Integer.parseInt(oAuthToken.getTenantId()));
        preparedStatement.setString(27, oAuthToken.getUserDomain());
        preparedStatement.setString(28, oAuthToken.getUserType());
        preparedStatement.setString(29, oAuthToken.getGrantType());
        preparedStatement.setTimestamp(30, Timestamp.valueOf(oAuthToken.getTimeCreated()));
        preparedStatement.setTimestamp(31,
                Timestamp.valueOf(oAuthToken.getRefreshTokenTimeCreated()));
        preparedStatement.setInt(32, Integer.parseInt(oAuthToken.getValidityPeriod()));
        preparedStatement.setInt(33,
                Integer.parseInt(oAuthToken.getRefreshTokenValidityPeriod()));
        preparedStatement.setString(34, oAuthToken.getTokenScopeHash());
        preparedStatement.setString(35, oAuthToken.getTokenState());
        preparedStatement.setString(36, oAuthToken.getTokenStateId());
        preparedStatement.setString(37, oAuthToken.getSubjectIdentifier());
        preparedStatement.setString(38, oAuthToken.getAccessTokenHash());
        preparedStatement.setString(39, oAuthToken.getRefreshTokenHash());
        preparedStatement.setInt(40, Integer.parseInt(oAuthToken.getIdpId()));
        preparedStatement.setString(41, oAuthToken.getTokenBindingRef());
    }
}

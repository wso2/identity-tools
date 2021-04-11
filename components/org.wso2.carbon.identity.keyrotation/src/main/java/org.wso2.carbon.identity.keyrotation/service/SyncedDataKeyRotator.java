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

package org.wso2.carbon.identity.keyrotation.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.wso2.carbon.identity.keyrotation.config.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.dao.DBConstants;
import org.wso2.carbon.identity.keyrotation.dao.IdentityDAO;
import org.wso2.carbon.identity.keyrotation.dao.OAuthDAO;
import org.wso2.carbon.identity.keyrotation.model.TempOAuthCode;
import org.wso2.carbon.identity.keyrotation.model.TempOAuthScope;
import org.wso2.carbon.identity.keyrotation.model.TempOAuthToken;
import org.wso2.carbon.identity.keyrotation.model.TempTOTPSecret;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.wso2.carbon.identity.keyrotation.util.EncryptionUtil.checkPlainText;
import static org.wso2.carbon.identity.keyrotation.util.EncryptionUtil.symmetricReEncryption;

/**
 * This class holds the synced data re-encryption service.
 */
public class SyncedDataKeyRotator {

    private static final Logger log = Logger.getLogger(SyncedDataKeyRotator.class);
    private static final SyncedDataKeyRotator instance = new SyncedDataKeyRotator();
    private static int totpIndex = 1;
    private static int codeIndex = 1;
    private static int tokenIndex = 1;
    private static int scopeIndex = 1;

    public static SyncedDataKeyRotator getInstance() {

        return instance;
    }

    /**
     * Re-encryption of the synced data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while re-encrypting synced data.
     */
    public void syncedDataReEncryptor(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.info("Started re-encrypting synced data...");
        try {
            while (true) {
                transformTempIdentityTOTPData(keyRotationConfig);
                log.info("Successfully transformed totp data records in IDN_IDENTITY_USER_DATA_TEMP: " +
                        IdentityDAO.insertCount);
                log.info("Transformation failed totp data records in IDN_IDENTITY_USER_DATA_TEMP: " +
                        IdentityDAO.failedInsertCount);
                transformTempOauthCodeData(keyRotationConfig);
                log.info("Successfully transformed OAuth code data records in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP: " +
                        OAuthDAO.insertCodeCount);
                log.info("Transformation failed OAuth code data records in IDN_OAUTH2_AUTHORIZATION_CODE_TEMP: " +
                        OAuthDAO.failedInsertCodeCount);
                transformTempOauthTokenData(keyRotationConfig);
                log.info("Successfully transformed OAuth token data records in IDN_OAUTH2_ACCESS_TOKEN_TEMP: " +
                        OAuthDAO.insertTokenCount);
                log.info("Transformation failed OAuth token data records in IDN_OAUTH2_ACCESS_TOKEN_TEMP: " +
                        OAuthDAO.failedInsertTokenCount);
                transformTempOauthScopeData(keyRotationConfig);
                log.info("Successfully transformed OAuth scope data records in IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP: " +
                        OAuthDAO.insertScopeCount);
                log.info("Transformation failed OAuth scope data records in IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP: " +
                        OAuthDAO.failedInsertScopeCount);
                log.debug("Sleeping...\n");
                TimeUnit.MILLISECONDS.sleep(1000);
                log.debug("Awake...\n");
            }
        } catch (InterruptedException e) {
            throw new KeyRotationException("Error while thread waiting, sleeping or being occupied.", e);
        }
    }

    /**
     * Transformation of the IDN_IDENTITY_USER_DATA_TEMP table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while transforming TOTP data.
     */
    private void transformTempIdentityTOTPData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.debug("Started transformation of the TOTP data...");
        List<TempTOTPSecret> records = IdentityDAO.getInstance().getTempTOTPSecrets(totpIndex, keyRotationConfig);
        while (CollectionUtils.isNotEmpty(records)) {
            TempTOTPSecret record = records.get(0);
            log.debug("RECORD " + record.getSyncId());
            if (record.getSynced() == 0) {
                TempTOTPSecret latestRecord =
                        IdentityDAO.getInstance().getTempTOTPLatest(record, keyRotationConfig).get(0);
                log.debug("latestRecord " + latestRecord.getSyncId());
                List<TempTOTPSecret> previousSimilarRecords =
                        IdentityDAO.getInstance().getTempTOTPPrevious(latestRecord, keyRotationConfig);
                for (TempTOTPSecret previousSimilarRecord : previousSimilarRecords) {
                    previousSimilarRecord.setSynced(1);
                    log.debug("previousSimilarRecords " + previousSimilarRecord.getSyncId());
                }
                IdentityDAO.getInstance().updateTOTPPreviousSimilarRecords(previousSimilarRecords, keyRotationConfig);
                if ((DBConstants.SECRET_KEY.equals(latestRecord.getDataKey()) ||
                        DBConstants.VERIFIED_SECRET_KEY.equals(latestRecord.getDataKey())) &&
                        latestRecord.getAvailability() == 1 &&
                        !checkPlainText(latestRecord.getDataValue())) {
                    log.debug("SYNC_ID " + totpIndex + " " + latestRecord.getSyncId());
                    log.debug("Encrypted value " + latestRecord.getDataValue());
                    String reEncryptedValue = symmetricReEncryption(latestRecord.getDataValue(), keyRotationConfig);
                    latestRecord.setDataValue(reEncryptedValue);
                    log.debug("Re-encrypted value " + latestRecord.getDataValue());
                }
                if (latestRecord.getAvailability() == 1) {
                    int updatedRecords = IdentityDAO.getInstance().updateTOTPSecret(latestRecord, keyRotationConfig);
                    if (updatedRecords == 0) {
                        IdentityDAO.getInstance().insertTOTPSecret(latestRecord, keyRotationConfig);
                    }
                } else if (latestRecord.getAvailability() == 0) {
                    IdentityDAO.getInstance().deleteTOTPSecret(latestRecord, keyRotationConfig);
                }
            }
            totpIndex++;
            records = IdentityDAO.getInstance().getTempTOTPSecrets(totpIndex, keyRotationConfig);
        }
    }

    /**
     * Transformation of the IDN_OAUTH2_AUTHORIZATION_CODE_TEMP table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while transforming OAuth2 authorization code data.
     */
    private void transformTempOauthCodeData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.debug("Started transformation of the OAuth2 authorization code data...");
        List<TempOAuthCode> records = OAuthDAO.getInstance().getTempOAuthCode(codeIndex, keyRotationConfig);
        while (CollectionUtils.isNotEmpty(records)) {
            TempOAuthCode record = records.get(0);
            log.debug("RECORD " + record.getSyncId());
            if (record.getSynced() == 0) {
                TempOAuthCode latestRecord =
                        OAuthDAO.getInstance().getTempOAuthCodeLatest(record, keyRotationConfig).get(0);
                log.debug("latestRecord " + latestRecord.getSyncId());
                List<TempOAuthCode> previousSimilarRecords =
                        OAuthDAO.getInstance().getTempOAuthCodePrevious(latestRecord, keyRotationConfig);
                for (TempOAuthCode previousSimilarRecord : previousSimilarRecords) {
                    previousSimilarRecord.setSynced(1);
                    log.debug("previousSimilarRecords " + previousSimilarRecord.getSyncId());
                }
                OAuthDAO.getInstance().updateCodePreviousSimilarRecords(previousSimilarRecords, keyRotationConfig);
                if (latestRecord.getAvailability() == 1 && !checkPlainText(latestRecord.getAuthorizationCode())) {
                    log.debug("SYNC_ID " + codeIndex + " " + latestRecord.getSyncId());
                    log.debug("Encrypted value " + latestRecord.getAuthorizationCode());
                    String reEncryptedValue =
                            symmetricReEncryption(latestRecord.getAuthorizationCode(), keyRotationConfig);
                    latestRecord.setAuthorizationCode(reEncryptedValue);
                    log.debug("Re-encrypted value " + latestRecord.getAuthorizationCode());
                }
                if (latestRecord.getAvailability() == 1) {
                    int updatedRecords = OAuthDAO.getInstance().updateOAuthCode(latestRecord, keyRotationConfig);
                    if (updatedRecords == 0) {
                        OAuthDAO.getInstance().insertOAuthCode(latestRecord, keyRotationConfig);
                    }
                } else if (latestRecord.getAvailability() == 0) {
                    OAuthDAO.getInstance().deleteOAuthCode(latestRecord, keyRotationConfig);
                }
            }
            codeIndex++;
            records = OAuthDAO.getInstance().getTempOAuthCode(codeIndex, keyRotationConfig);
        }
    }

    /**
     * Transformation of the IDN_OAUTH2_ACCESS_TOKEN_TEMP table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while transforming OAuth2 access and refresh token data.
     */
    private void transformTempOauthTokenData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.debug("Started transformation of the OAuth2 access and refresh tokens data...");
        List<TempOAuthToken> records = OAuthDAO.getInstance().getTempOAuthToken(tokenIndex, keyRotationConfig);
        while (CollectionUtils.isNotEmpty(records)) {
            TempOAuthToken record = records.get(0);
            log.debug("RECORD " + record.getSyncId());
            if (record.getSynced() == 0) {
                TempOAuthToken latestRecord =
                        OAuthDAO.getInstance().getTempOAuthTokenLatest(record, keyRotationConfig).get(0);
                log.debug("latestRecord " + latestRecord.getSyncId());
                List<TempOAuthToken> previousSimilarRecords =
                        OAuthDAO.getInstance().getTempOAuthTokenPrevious(latestRecord, keyRotationConfig);
                for (TempOAuthToken previousSimilarRecord : previousSimilarRecords) {
                    previousSimilarRecord.setSynced(1);
                    log.debug("previousSimilarRecords " + previousSimilarRecord.getSyncId());
                }
                OAuthDAO.getInstance().updateTokenPreviousSimilarRecords(previousSimilarRecords, keyRotationConfig);
                if (latestRecord.getAvailability() == 1 && !checkPlainText(latestRecord.getAccessToken()) &&
                        !checkPlainText(latestRecord.getRefreshToken())) {
                    log.debug("SYNC_ID " + tokenIndex + " " + latestRecord.getSyncId());
                    log.debug("Encrypted access token value " + latestRecord.getAccessToken());
                    String accessTokenReEncryptedValue =
                            symmetricReEncryption(latestRecord.getAccessToken(), keyRotationConfig);
                    latestRecord.setAccessToken(accessTokenReEncryptedValue);
                    log.debug("Re-encrypted value " + latestRecord.getAccessToken());
                    log.debug("Encrypted refresh token value " + latestRecord.getRefreshToken());
                    String refreshTokenReEncryptedValue =
                            symmetricReEncryption(latestRecord.getRefreshToken(), keyRotationConfig);
                    latestRecord.setRefreshToken(refreshTokenReEncryptedValue);
                    log.debug("Re-encrypted value " + latestRecord.getRefreshToken());
                }
                if (latestRecord.getAvailability() == 1) {
                    int updatedRecords = OAuthDAO.getInstance().updateOAuthToken(latestRecord, keyRotationConfig);
                    if (updatedRecords == 0) {
                        OAuthDAO.getInstance().insertOAuthToken(latestRecord, keyRotationConfig);
                    }
                } else if (latestRecord.getAvailability() == 0) {
                    OAuthDAO.getInstance().deleteOAuthToken(latestRecord, keyRotationConfig);
                }
            }
            tokenIndex++;
            records = OAuthDAO.getInstance().getTempOAuthToken(tokenIndex, keyRotationConfig);
        }
    }

    /**
     * Transformation of the IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while transforming OAuth2 scope data.
     */
    private void transformTempOauthScopeData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.debug("Started transformation of the OAuth2 scope data...");
        List<TempOAuthScope> records = OAuthDAO.getInstance().getTempOAuthScope(scopeIndex, keyRotationConfig);
        while (CollectionUtils.isNotEmpty(records)) {
            TempOAuthScope record = records.get(0);
            log.debug("RECORD " + record.getSyncId());
            if (record.getSynced() == 0) {
                TempOAuthScope latestRecord =
                        OAuthDAO.getInstance().getTempOAuthScopeLatest(record, keyRotationConfig).get(0);
                log.debug("latestRecord " + latestRecord.getSyncId());
                List<TempOAuthScope> previousSimilarRecords =
                        OAuthDAO.getInstance().getTempOAuthScopePrevious(latestRecord, keyRotationConfig);
                for (TempOAuthScope previousSimilarRecord : previousSimilarRecords) {
                    previousSimilarRecord.setSynced(1);
                    log.debug("previousSimilarRecords " + previousSimilarRecord.getSyncId());
                }
                OAuthDAO.getInstance().updateScopePreviousSimilarRecords(previousSimilarRecords, keyRotationConfig);
                if (latestRecord.getAvailability() == 1) {
                    int updatedRecords = OAuthDAO.getInstance().updateOAuthScope(latestRecord, keyRotationConfig);
                    if (updatedRecords == 0) {
                        OAuthDAO.getInstance().insertOAuthScope(latestRecord, keyRotationConfig);
                    }
                } else if (latestRecord.getAvailability() == 0) {
                    OAuthDAO.getInstance().deleteOAuthScope(latestRecord, keyRotationConfig);
                }
            }
            scopeIndex++;
            records = OAuthDAO.getInstance().getTempOAuthScope(scopeIndex, keyRotationConfig);
        }
    }
}

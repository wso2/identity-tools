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
package org.wso2.carbon.identity.keyrotation;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.identity.keyrotation.config.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.dao.IdentityDAO;
import org.wso2.carbon.identity.keyrotation.dao.IdentitySecret;
import org.wso2.carbon.identity.keyrotation.dao.OAuthCode;
import org.wso2.carbon.identity.keyrotation.dao.OAuthDAO;
import org.wso2.carbon.identity.keyrotation.dao.OAuthSecrets;
import org.wso2.carbon.identity.keyrotation.dao.OAuthTokens;
import org.wso2.carbon.identity.keyrotation.util.CryptoProvider;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.util.List;

import static org.wso2.carbon.identity.keyrotation.dao.DBConstants.CHUNK_SIZE;
import static org.wso2.carbon.identity.keyrotation.dao.DBConstants.DATA_KEY;
import static org.wso2.carbon.identity.keyrotation.dao.DBConstants.TEST_APP_NAME;
import static org.wso2.carbon.identity.keyrotation.dao.DBConstants.TEST_CONSUMER_KEY_ID;

/**
 * Class that calls the key-rotation service.
 */
public class KeyRotationService {

    private static final Log log = LogFactory.getLog(KeyRotationService.class);
    private static final KeyRotationService instance = new KeyRotationService();

    public static KeyRotationService getInstance() {

        return instance;
    }

    public static void main(String[] args) throws KeyRotationException {

        KeyRotationConfig configs = KeyRotationConfig.loadConfigs();
        KeyRotationService.getInstance().reEncryptDBDump(configs);
        KeyRotationService.getInstance().reEncryptConfigFiles(configs.getOldISHome());
        KeyRotationService.getInstance().reEncryptSyncedData(configs);

    }

    /**
     * ReEncryption mechanism needed for the key rotation service.
     *
     * @param cipher            The ciphertext needed to perform re-encryption on.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return Decrypted from old key and encrypted from new key.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */

    private String reEncryptor(String cipher, KeyRotationConfig keyRotationConfig)
            throws KeyRotationException {

        CryptoProvider cryptoProvider = new CryptoProvider();
        byte[] refactoredCipher = cryptoProvider.reFactorCipherText(Base64.decode(cipher));
        byte[] plainText = cryptoProvider.decrypt(refactoredCipher, keyRotationConfig);
        byte[] cipherText = cryptoProvider.encrypt(plainText, keyRotationConfig);
        return Base64.encode(cipherText);
    }

    /**
     * ReEncryption of the DB data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    private void reEncryptDBDump(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.info("Re-encrypting DB data...");
        reEncryptIdentityData(keyRotationConfig);
        reEncryptOauthAuthData(keyRotationConfig);
        reEncryptOauthTokenData(keyRotationConfig);
        reEncryptOauthConsumerData(keyRotationConfig);
        reEncryptBPSData(keyRotationConfig);
        reEncryptWFRequestData(keyRotationConfig);
        log.info("Re-encrypting DB data completed...");

    }

    /**
     * ReEncryption of the configuration file data.
     *
     * @param isHomePath Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    private void reEncryptConfigFiles(String isHomePath) throws KeyRotationException {

        log.info("Re-encrypting configuration file data...");

        log.info("Re-encrypting configuration file data completed...");
    }

    /**
     * ReEncryption of the synced data in temporary tables.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    private void reEncryptSyncedData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.info("Re-encrypting synced data...");

        log.info("Re-encrypting synced data completed...");

    }

    /**
     * ReEncryption of the IDN_IDENTITY_USER_DATA table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    private void reEncryptIdentityData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.info("Re-encryption of the Identity data...");
        int startIndex = 0;
        List<IdentitySecret> chunkList =
                IdentityDAO.getInstance().getIdentitySecretsChunks(startIndex, keyRotationConfig);
        while (chunkList.size() > 0) {
            for (IdentitySecret identitySecret : chunkList) {
                if (identitySecret.getDataKey().equals(DATA_KEY)) {
                    log.info("Old " + identitySecret.getDataValue());
                    String reEncryptedValue = reEncryptor(identitySecret.getDataValue(), keyRotationConfig);
                    identitySecret.setDataValue(reEncryptedValue);
                    log.info("New " + identitySecret.getDataValue() + "\n");
                    IdentityDAO.getInstance().updateIdentitySecretsChunks(chunkList, keyRotationConfig);
                }
            }
            startIndex = startIndex + CHUNK_SIZE;
            chunkList = IdentityDAO.getInstance().getIdentitySecretsChunks(startIndex, keyRotationConfig);
        }
    }

    /**
     * ReEncryption of the IDN_OAUTH2_AUTHORIZATION_CODE table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    private void reEncryptOauthAuthData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.info("Re-encryption of the Oauth2 authorization code data...");
        int startIndex = 0;
        List<OAuthCode> chunkList =
                OAuthDAO.getInstance().getOAuthCodeChunks(startIndex, keyRotationConfig);
        while (chunkList.size() > 0) {
            for (OAuthCode oAuthCode : chunkList) {
                //this condition is only for testing purposes
                if (oAuthCode.getConsumerKeyId().equals(TEST_CONSUMER_KEY_ID)) {
                    log.info("Old " + oAuthCode.getAuthorizationCode());
                    String reEncryptedValue = reEncryptor(oAuthCode.getAuthorizationCode(),
                            keyRotationConfig);
                    oAuthCode.setAuthorizationCode(reEncryptedValue);
                    log.info("New " + oAuthCode.getAuthorizationCode() + "\n");
                    OAuthDAO.getInstance().updateOAuthCodeChunks(chunkList, keyRotationConfig);
                }
            }
            startIndex = startIndex + CHUNK_SIZE;
            chunkList = OAuthDAO.getInstance().getOAuthCodeChunks(startIndex, keyRotationConfig);
        }

    }

    /**
     * ReEncryption of the IDN_OAUTH2_ACCESS_TOKEN table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    private void reEncryptOauthTokenData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.info("Re-encryption of the Oauth2 access token data...");
        int startIndex = 0;
        List<OAuthTokens> chunkList =
                OAuthDAO.getInstance().getOAuthTokenChunks(startIndex, keyRotationConfig);
        while (chunkList.size() > 0) {
            for (OAuthTokens oAuthTokens : chunkList) {
                //this condition is only for testing purposes
                if (oAuthTokens.getConsumerKeyId().equals(TEST_CONSUMER_KEY_ID)) {
                    log.info("Old " + oAuthTokens.getAccessToken());
                    String reEncryptedValue = reEncryptor(oAuthTokens.getAccessToken(),
                            keyRotationConfig);
                    oAuthTokens.setAccessToken(reEncryptedValue);
                    log.info("New " + oAuthTokens.getAccessToken() + "\n");
                    OAuthDAO.getInstance().updateOAuthTokenChunks(chunkList, keyRotationConfig);
                }
            }
            startIndex = startIndex + CHUNK_SIZE;
            chunkList = OAuthDAO.getInstance().getOAuthTokenChunks(startIndex, keyRotationConfig);
        }

    }

    /**
     * ReEncryption of the IDN_OAUTH_CONSUMER_APPS consumer table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    private void reEncryptOauthConsumerData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.info("Re-encryption of the Oauth consumer data...");
        int startIndex = 0;
        List<OAuthSecrets> chunkList =
                OAuthDAO.getInstance().getOAuthSecretChunks(startIndex, keyRotationConfig);
        while (chunkList.size() > 0) {
            for (OAuthSecrets oAuthSecrets : chunkList) {
                //this condition is only for testing purposes
                if (oAuthSecrets.getAppName().equals(TEST_APP_NAME)) {
                    log.info("Old " + oAuthSecrets.getConsumerSecret());
                    String reEncryptedValue = reEncryptor(oAuthSecrets.getConsumerSecret(),
                            keyRotationConfig);
                    oAuthSecrets.setConsumerSecret(reEncryptedValue);
                    log.info("New " + oAuthSecrets.getConsumerSecret() + "\n");
                }
            }
            startIndex = startIndex + CHUNK_SIZE;
            chunkList = OAuthDAO.getInstance().getOAuthSecretChunks(startIndex, keyRotationConfig);
        }

    }

    /**
     * ReEncryption of the WF_BPS_PROFILE table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    private void reEncryptBPSData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.info("Re-encryption of the BPS profile data...");

    }

    /**
     * ReEncryption of the WF_REQUEST table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    private void reEncryptWFRequestData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.info("Re-encryption of the WF request data...");

    }
}

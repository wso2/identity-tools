/*
 * Copyright (c) (2021-2024), WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.wso2.carbon.identity.keyrotation.config.model.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.dao.BPSProfileDAO;
import org.wso2.carbon.identity.keyrotation.dao.IdentityDAO;
import org.wso2.carbon.identity.keyrotation.dao.OAuthDAO;
import org.wso2.carbon.identity.keyrotation.dao.RegistryDAO;
import org.wso2.carbon.identity.keyrotation.dao.WorkFlowDAO;
import org.wso2.carbon.identity.keyrotation.model.BPSPassword;
import org.wso2.carbon.identity.keyrotation.model.IdentitySecret;
import org.wso2.carbon.identity.keyrotation.model.OAuthCode;
import org.wso2.carbon.identity.keyrotation.model.OAuthSecret;
import org.wso2.carbon.identity.keyrotation.model.OAuthToken;
import org.wso2.carbon.identity.keyrotation.model.RegistryProperty;
import org.wso2.carbon.identity.keyrotation.model.TOTPSecret;
import org.wso2.carbon.identity.keyrotation.model.WorkFlowRequestSecret;
import org.wso2.carbon.identity.keyrotation.model.XACMLSubscriberSecret;
import org.wso2.carbon.identity.keyrotation.util.EncryptionUtil;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.keyrotation.util.EncryptionUtil.checkPlainText;
import static org.wso2.carbon.identity.keyrotation.util.EncryptionUtil.symmetricReEncryption;

/**
 * This class holds the DB re-encryption service.
 */
public class DBKeyRotator {

    private static final Logger log = Logger.getLogger(DBKeyRotator.class);
    private static final DBKeyRotator instance = new DBKeyRotator();

    public static DBKeyRotator getInstance() {

        return instance;
    }

    /**
     * Re-encryption of the identity and registry DB data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while re-encrypting identity and registry DB data.
     */
    public void dbReEncryptor(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.info("Started re-encrypting identity and registry DB data...");

        reEncryptIdentityTOTPData(keyRotationConfig);
        log.info("Successfully updated totp data records in IDN_IDENTITY_USER_DATA: " + IdentityDAO.updateCount);
        log.info("Failed totp data records in IDN_IDENTITY_USER_DATA: " + IdentityDAO.failedUpdateCount);

        reEncryptOauthAuthData(keyRotationConfig);
        log.info("Successfully updated OAuth2 authorization code data records in IDN_OAUTH2_AUTHORIZATION_CODE: " +
                OAuthDAO.updateCodeCount);
        log.info("Failed OAuth2 authorization code data records in IDN_OAUTH2_AUTHORIZATION_CODE: " +
                OAuthDAO.failedUpdateCodeCount);

        reEncryptOauthTokenData(keyRotationConfig);
        log.info("Successfully updated OAuth2 access and refresh tokens data records in IDN_OAUTH2_ACCESS_TOKEN: " +
                OAuthDAO.updateTokenCount);
        log.info("Failed OAuth2 access and refresh tokens data records in IDN_OAUTH2_ACCESS_TOKEN: " +
                OAuthDAO.failedUpdateTokenCount);

        reEncryptOauthConsumerData(keyRotationConfig);
        log.info("Successfully updated OAuth consumer secret data records in IDN_OAUTH_CONSUMER_APPS: " +
                OAuthDAO.updateSecretCount);
        log.info("Failed OAuth consumer secret data records in IDN_OAUTH_CONSUMER_APPS: " +
                OAuthDAO.failedUpdateSecretCount);

        reEncryptKeystorePasswordData(keyRotationConfig);
        log.info("Successfully updated keystore password property data records in REG_PROPERTY: " +
                RegistryDAO.updateCount);
        log.info("Failed keystore password property data records in REG_PROPERTY: " + RegistryDAO.failedUpdateCount);

        reEncryptKeystorePrivatekeyPassData(keyRotationConfig);
        log.info("Successfully updated keystore private key passphrase property data records in REG_PROPERTY: " +
                RegistryDAO.updateCount);
        log.info("Failed keystore private key passphrase property data records in REG_PROPERTY: " +
                RegistryDAO.failedUpdateCount);

        reEncryptSubscriberPasswordData(keyRotationConfig);
        log.info("Successfully updated subscriber password property data records in REG_PROPERTY: " +
                RegistryDAO.updateCount);
        log.info("Failed subscriber password property data records in REG_PROPERTY: " + RegistryDAO.failedUpdateCount);

        reEncryptIdentitySecrets(keyRotationConfig);
        log.info("Successfully updated Identity secret data records in IDN_SECRET: " +
                IdentityDAO.updateIdentitySecretCount);
        log.info("Failed Identity secret data records in IDN_SECRET: " +
                IdentityDAO.failedUpdateIdentitySecretCount);

        reEncryptXACMLSubscriberSecrets(keyRotationConfig);
        log.info("Successfully updated XAML subscriber secret data records in IDN_XACML_SUBSCRIBER_PROPERTY: " +
                IdentityDAO.updateXACMLSecretCount);
        log.info("Failed Identity secret data records in IDN_XACML_SUBSCRIBER_PROPERTY: " +
                IdentityDAO.failedXACMLSecretCount);

        if (keyRotationConfig.isEnableWorkflowMigrator()) {

            reEncryptBPSData(keyRotationConfig);
            log.info("Successfully updated BPS profile data records in WF_BPS_PROFILE: " + BPSProfileDAO.updateCount);
            log.info("Failed BPS profile data records in WF_BPS_PROFILE: " + BPSProfileDAO.failedUpdateCount);

            reEncryptWFRequestData(keyRotationConfig);
            log.info("Successfully updated WF request data records in WF_REQUEST: " + WorkFlowDAO.updateCount);
            log.info("Failed WF request data records in WF_REQUEST: " + WorkFlowDAO.failedUpdateCount);
        }

        log.info("Finished re-encrypting identity and registry DB data completed...\n");
    }

    /**
     * Re-encryption of the IDN_IDENTITY_USER_DATA table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while re-encrypting TOTP data.
     */
    public void reEncryptIdentityTOTPData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.debug("Started re-encryption of the TOTP data...");
        int startIndex = 0;
        List<TOTPSecret> chunkList =
                IdentityDAO.getInstance().getTOTPSecretsChunks(startIndex, keyRotationConfig);


        while (CollectionUtils.isNotEmpty(chunkList)) {

            EncryptionUtil.writeBackup("IDN_IDENTITY_USER_DATA",
                    IdentityDAO.getInstance().generateTOTPBackup(chunkList));

            List<TOTPSecret> midChunkList = new ArrayList<>();
            for (TOTPSecret totpSecret : chunkList) {
                if (!checkPlainText(totpSecret.getDataValue())) {
                    log.debug("Encrypted value " + totpSecret.getDataValue());
                    String reEncryptedValue = symmetricReEncryption(totpSecret.getDataValue(), keyRotationConfig);
                    totpSecret.setNewDataValue(reEncryptedValue);
                    log.debug("Re-encrypted value " + totpSecret.getDataValue());
                    midChunkList.add(totpSecret);
                }
            }
            IdentityDAO.getInstance().updateTOTPSecretsChunks(midChunkList, keyRotationConfig);
            startIndex = startIndex + keyRotationConfig.getChunkSize();
            chunkList = IdentityDAO.getInstance().getTOTPSecretsChunks(startIndex, keyRotationConfig);
        }
        log.debug("Finished re-encryption of the TOTP data...");
    }

    /**
     * Re-encryption of the IDN_OAUTH2_AUTHORIZATION_CODE table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while re-encrypting OAuth2 authorization code data.
     */
    public void reEncryptOauthAuthData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.debug("Started re-encryption of the OAuth2 authorization code data...");
        int startIndex = 0;
        List<OAuthCode> chunkList =
                OAuthDAO.getInstance().getOAuthCodeChunks(startIndex, keyRotationConfig);


        while (CollectionUtils.isNotEmpty(chunkList)) {

            EncryptionUtil.writeBackup("IDN_OAUTH2_AUTHORIZATION_CODE",
                    OAuthDAO.getInstance().generateOAuthCodeBackup(chunkList));

            List<OAuthCode> midChunkList = new ArrayList<>();
            for (OAuthCode oAuthCode : chunkList) {
                if (!checkPlainText(oAuthCode.getAuthorizationCode())) {
                    log.debug("Encrypted value " + oAuthCode.getAuthorizationCode());
                    String reEncryptedValue = symmetricReEncryption(oAuthCode.getAuthorizationCode(),
                            keyRotationConfig);
                    oAuthCode.setNewAuthorizationCode(reEncryptedValue);
                    log.debug("Re-encrypted value " + oAuthCode.getAuthorizationCode());
                    midChunkList.add(oAuthCode);
                }
            }
            OAuthDAO.getInstance().updateOAuthCodeChunks(midChunkList, keyRotationConfig);
            startIndex = startIndex + keyRotationConfig.getChunkSize();
            chunkList = OAuthDAO.getInstance().getOAuthCodeChunks(startIndex, keyRotationConfig);
        }
        log.debug("Finished re-encryption of the OAuth2 authorization code data...");
    }

    /**
     * Re-encryption of the IDN_OAUTH2_ACCESS_TOKEN table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while re-encrypting OAuth2 access and refresh token data.
     */
    public void reEncryptOauthTokenData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.debug("Started re-encryption of the OAuth2 access and refresh token data...");
        int startIndex = 0;
        List<OAuthToken> chunkList =
                OAuthDAO.getInstance().getOAuthTokenChunks(startIndex, keyRotationConfig);


        while (CollectionUtils.isNotEmpty(chunkList)) {

            EncryptionUtil.writeBackup("IDN_OAUTH2_ACCESS_TOKEN",
                    OAuthDAO.getInstance().generateOAuthTokenBackup(chunkList));

            List<OAuthToken> midChunkList = new ArrayList<>();
            for (OAuthToken oAuthToken : chunkList) {
                if (!checkPlainText(oAuthToken.getAccessToken()) && !checkPlainText(oAuthToken.getRefreshToken())) {
                    log.debug("Encrypted access token value " + oAuthToken.getAccessToken());
                    String accessTokenReEncryptedValue = symmetricReEncryption(oAuthToken.getAccessToken(),
                            keyRotationConfig);
                    oAuthToken.setNewAccessToken(accessTokenReEncryptedValue);
                    log.debug("Re-encrypted value " + oAuthToken.getAccessToken());
                    log.debug("Encrypted refresh token value " + oAuthToken.getRefreshToken());
                    String refreshTokenReEncryptedValue = symmetricReEncryption(oAuthToken.getRefreshToken(),
                            keyRotationConfig);
                    oAuthToken.setNewRefreshToken(refreshTokenReEncryptedValue);
                    log.debug("Re-encrypted value " + oAuthToken.getRefreshToken());
                    midChunkList.add(oAuthToken);
                }
            }
            OAuthDAO.getInstance().updateOAuthTokenChunks(midChunkList, keyRotationConfig);
            startIndex = startIndex + keyRotationConfig.getChunkSize();
            chunkList = OAuthDAO.getInstance().getOAuthTokenChunks(startIndex, keyRotationConfig);
        }
        log.debug("Finished re-encryption of the OAuth2 access and refresh token data...");
    }

    /**
     * Re-encryption of the IDN_OAUTH_CONSUMER_APPS consumer table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while re-encrypting OAuth consumer secret data.
     */
    public void reEncryptOauthConsumerData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.debug("Started re-encryption of the OAuth consumer secret data...");
        int startIndex = 0;
        List<OAuthSecret> chunkList =
                OAuthDAO.getInstance().getOAuthSecretChunks(startIndex, keyRotationConfig);


        while (CollectionUtils.isNotEmpty(chunkList)) {

            EncryptionUtil.writeBackup("IDN_OAUTH_CONSUMER_APPS",
                    OAuthDAO.getInstance().generateOAuthConsumerBackup(chunkList));

            List<OAuthSecret> midChunkList = new ArrayList<>();
            for (OAuthSecret oAuthSecret : chunkList) {
                if (!checkPlainText(oAuthSecret.getConsumerSecret())) {
                    log.debug("Encrypted value " + oAuthSecret.getConsumerSecret());
                    String reEncryptedValue = symmetricReEncryption(oAuthSecret.getConsumerSecret(),
                            keyRotationConfig);
                    if (StringUtils.isEmpty(reEncryptedValue)) {
                        log.debug("Skipping since the value is already encrypted with new secret");
                        continue;
                    }
                    oAuthSecret.setNewConsumerSecret(reEncryptedValue);
                    log.debug("Re-encrypted value " + oAuthSecret.getConsumerSecret());
                    midChunkList.add(oAuthSecret);
                }
            }
            OAuthDAO.getInstance().updateOAuthSecretChunks(midChunkList, keyRotationConfig);
            startIndex = startIndex + keyRotationConfig.getChunkSize();
            chunkList = OAuthDAO.getInstance().getOAuthSecretChunks(startIndex, keyRotationConfig);
        }
        log.debug("Finished re-encryption of the OAuth consumer secret data...");
    }

    /**
     * Re-encryption of the WF_BPS_PROFILE table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while re-encrypting BPS profile data.
     */
    public void reEncryptBPSData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.debug("Started re-encryption of the BPS profile data...");
        int startIndex = 0;
        List<BPSPassword> chunkList =
                BPSProfileDAO.getInstance().getBpsPasswordChunks(startIndex, keyRotationConfig);

        while (CollectionUtils.isNotEmpty(chunkList)) {

            EncryptionUtil.writeBackup("WF_BPS_PROFILE",
                    BPSProfileDAO.getInstance().generateWorkflowBPSProfileBackup(chunkList));

            List<BPSPassword> midChunkList = new ArrayList<>();
            for (BPSPassword bpsPassword : chunkList) {
                if (!checkPlainText(bpsPassword.getPassword())) {
                    log.debug("Encrypted value " + bpsPassword.getPassword());
                    String reEncryptedValue = symmetricReEncryption(bpsPassword.getPassword(), keyRotationConfig);
                    bpsPassword.setNewPassword(reEncryptedValue);
                    log.debug("Re-encrypted value " + bpsPassword.getPassword());
                    midChunkList.add(bpsPassword);
                }
            }
            BPSProfileDAO.getInstance().updateBpsPasswordChunks(midChunkList, keyRotationConfig);
            startIndex = startIndex + keyRotationConfig.getChunkSize();
            chunkList = BPSProfileDAO.getInstance().getBpsPasswordChunks(startIndex, keyRotationConfig);
        }
        log.debug("Finished re-encryption of the BPS profile data...");
    }

    /**
     * Re-encryption of the WF_REQUEST table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while re-encrypting WF request data.
     */
    public void reEncryptWFRequestData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.debug("Started re-encryption of the WF request data...");
        int startIndex = 0;
        List<WorkFlowRequestSecret> chunkList =
                WorkFlowDAO.getInstance().getWFRequestChunks(startIndex, keyRotationConfig);


        while (CollectionUtils.isNotEmpty(chunkList)) {

            EncryptionUtil.writeBackup("WF_REQUEST",
                    WorkFlowDAO.getInstance().generateWorkflowRequestBackup(chunkList));

            List<WorkFlowRequestSecret> midChunkList = new ArrayList<>();
            for (WorkFlowRequestSecret workFlowRequestSecret : chunkList) {
                if (!checkPlainText(workFlowRequestSecret.getRequestSecret())) {
                    log.debug("Encrypted value " + workFlowRequestSecret.getRequestSecret());
                    String reEncryptedValue = symmetricReEncryption(workFlowRequestSecret.getRequestSecret(),
                            keyRotationConfig);
                    workFlowRequestSecret.setNewRequestSecret(reEncryptedValue);
                    log.debug("Re-encrypted value " + workFlowRequestSecret.getNewRequestSecret());
                    midChunkList.add(workFlowRequestSecret);
                }
            }
            WorkFlowDAO.getInstance().updateWFRequestChunks(midChunkList, keyRotationConfig);
            startIndex = startIndex + keyRotationConfig.getChunkSize();
            chunkList = WorkFlowDAO.getInstance().getWFRequestChunks(startIndex, keyRotationConfig);
        }
        log.debug("Finished re-encryption of the WF request data...");
    }

    /**
     * Re-encryption of keystore password in REG_PROPERTY table.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while re-encrypting keystore password property data.
     */
    public void reEncryptKeystorePasswordData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.debug("Started re-encryption of the keystore password property data...");
        RegistryDAO.updateCount = 0;
        RegistryDAO.failedUpdateCount = 0;
        int startIndex = 0;
        List<RegistryProperty> chunkList =
                RegistryDAO.getInstance().getRegPropertyDataChunks(startIndex, keyRotationConfig,
                        KeyRotationConstants.REGISTRY_PASSWORD);


        while (CollectionUtils.isNotEmpty(chunkList)) {

            EncryptionUtil.writeBackup("REG_PROPERTY",
                    RegistryDAO.getInstance().generateRegPropertyBackup(chunkList));

            List<RegistryProperty> midChunkList = new ArrayList<>();
            for (RegistryProperty regProperty : chunkList) {
                if (!checkPlainText(regProperty.getRegValue())) {
                    log.debug("Encrypted value " + regProperty.getRegValue());
                    String reEncryptedValue = symmetricReEncryption(regProperty.getRegValue(), keyRotationConfig);
                    regProperty.setNewRegValue(reEncryptedValue);
                    log.debug("Re-encrypted value " + regProperty.getRegValue());
                    midChunkList.add(regProperty);
                }
            }
            RegistryDAO.getInstance().updateRegPropertyDataChunks(midChunkList, keyRotationConfig,
                    KeyRotationConstants.REGISTRY_PASSWORD);
            startIndex = startIndex + keyRotationConfig.getChunkSize();
            chunkList = RegistryDAO.getInstance()
                    .getRegPropertyDataChunks(startIndex, keyRotationConfig, KeyRotationConstants.REGISTRY_PASSWORD);
        }
        log.debug("Finished re-encryption of the keystore password property data...");
    }

    /**
     * Re-encryption of keystore private key passphrase in REG_PROPERTY table.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while re-encrypting keystore private key passphrase property data.
     */
    public void reEncryptKeystorePrivatekeyPassData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.debug("Started re-encryption of the keystore privatekeyPass property data...");
        RegistryDAO.updateCount = 0;
        RegistryDAO.failedUpdateCount = 0;
        int startIndex = 0;
        List<RegistryProperty> chunkList =
                RegistryDAO.getInstance().getRegPropertyDataChunks(startIndex, keyRotationConfig,
                        KeyRotationConstants.PRIVATE_KEY_PASS);


        while (CollectionUtils.isNotEmpty(chunkList)) {

            EncryptionUtil.writeBackup("REG_PROPERTY",
                    RegistryDAO.getInstance().generateRegPropertyBackup(chunkList));

            List<RegistryProperty> midChunkList = new ArrayList<>();
            for (RegistryProperty regProperty : chunkList) {
                if (!checkPlainText(regProperty.getRegValue())) {
                    log.debug("Encrypted value " + regProperty.getRegValue());
                    String reEncryptedValue = symmetricReEncryption(regProperty.getRegValue(), keyRotationConfig);
                    regProperty.setNewRegValue(reEncryptedValue);
                    log.debug("Re-encrypted value " + regProperty.getRegValue());
                    midChunkList.add(regProperty);
                }
            }
            RegistryDAO.getInstance().updateRegPropertyDataChunks(midChunkList, keyRotationConfig,
                    KeyRotationConstants.PRIVATE_KEY_PASS);
            startIndex = startIndex + keyRotationConfig.getChunkSize();
            chunkList =
                    RegistryDAO.getInstance().getRegPropertyDataChunks(startIndex, keyRotationConfig,
                            KeyRotationConstants.PRIVATE_KEY_PASS);
        }
        log.debug("Finished re-encryption of the keystore private key passphrase property data...");
    }

    /**
     * Re-encryption of subscriber password in REG_PROPERTY table.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while re-encrypting subscriber password property data.
     */
    public void reEncryptSubscriberPasswordData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.debug("Started re-encryption of the subscriber password property data...");
        RegistryDAO.updateCount = 0;
        RegistryDAO.failedUpdateCount = 0;
        int startIndex = 0;
        List<RegistryProperty> chunkList =
                RegistryDAO.getInstance().getRegPropertyDataChunks(startIndex, keyRotationConfig,
                        KeyRotationConstants.SUBSCRIBER_PASSWORD);

        while (CollectionUtils.isNotEmpty(chunkList)) {

            EncryptionUtil.writeBackup("REG_PROPERTY",
                    RegistryDAO.getInstance().generateRegPropertyBackup(chunkList));

            List<RegistryProperty> midChunkList = new ArrayList<>();
            for (RegistryProperty regProperty : chunkList) {
                if (!checkPlainText(regProperty.getRegValue())) {
                    log.debug("Encrypted value " + regProperty.getRegValue());
                    String reEncryptedValue = symmetricReEncryption(regProperty.getRegValue(), keyRotationConfig);
                    regProperty.setNewRegValue(reEncryptedValue);
                    log.debug("Re-encrypted value " + regProperty.getRegValue());
                    midChunkList.add(regProperty);
                }
            }
            RegistryDAO.getInstance().updateRegPropertyDataChunks(midChunkList, keyRotationConfig,
                    KeyRotationConstants.SUBSCRIBER_PASSWORD);
            startIndex = startIndex + keyRotationConfig.getChunkSize();
            chunkList =
                    RegistryDAO.getInstance()
                            .getRegPropertyDataChunks(startIndex, keyRotationConfig,
                                    KeyRotationConstants.SUBSCRIBER_PASSWORD);
        }
        log.debug("Finished re-encryption of the subscriber password property data...");
    }

    /**
     * Re-encryption of the IDN_SECRET  table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while re-encrypting Identity secret data.
     */
    public void reEncryptIdentitySecrets(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.debug("Started re-encryption of the Identity secret data...");
        int startIndex = 0;
        List<IdentitySecret> chunkList =
                IdentityDAO.getInstance().getIdentitySecretChunks(startIndex, keyRotationConfig);


        while (CollectionUtils.isNotEmpty(chunkList)) {

            EncryptionUtil.writeBackup("IDN_SECRET",
                    IdentityDAO.getInstance().generateIdentitySecretBackup(chunkList));

            List<IdentitySecret> midChunkList = new ArrayList<>();
            for (IdentitySecret identitySecret : chunkList) {
                if (!checkPlainText(identitySecret.getSecretValue())) {
                    log.debug("Encrypted value " + identitySecret.getSecretValue());
                    String reEncryptedValue = symmetricReEncryption(identitySecret.getSecretValue(),
                            keyRotationConfig);
                    if (StringUtils.isEmpty(reEncryptedValue)) {
                        log.debug("Skipping since the value is already encrypted with new secret");
                        continue;
                    }
                    identitySecret.setNewSecretValue(reEncryptedValue);
                    log.debug("Re-encrypted value " + identitySecret.getNewSecretValue());
                    midChunkList.add(identitySecret);
                }
            }
            IdentityDAO.getInstance().updateIdentitySecretChunks(midChunkList, keyRotationConfig);
            startIndex = startIndex + keyRotationConfig.getChunkSize();
            chunkList = IdentityDAO.getInstance().getIdentitySecretChunks(startIndex, keyRotationConfig);
        }
        log.debug("Finished re-encryption of the Identity secret data...");
    }

    /**
     * Re-encryption of the IDN_XACML_SUBSCRIBER_PROPERTY  table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while re-encrypting XACML subscriber secret data.
     */
    public void reEncryptXACMLSubscriberSecrets(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.debug("Started re-encryption of the XACML subscriber secret data...");
        int startIndex = 0;
        List<XACMLSubscriberSecret> chunkList =
                IdentityDAO.getInstance()
                        .getXACMLSecretChunks(startIndex, keyRotationConfig, KeyRotationConstants.SUBSCRIBER_PASSWORD);

        while (CollectionUtils.isNotEmpty(chunkList)) {

            EncryptionUtil.writeBackup("IDN_XACML_SUBSCRIBER_PROPERTY",
                    IdentityDAO.getInstance().generateXACMLSubscriberBackup(chunkList));

            List<XACMLSubscriberSecret> midChunkList = new ArrayList<>();
            for (XACMLSubscriberSecret xacmlSubscriberSecret : chunkList) {
                if (!checkPlainText(xacmlSubscriberSecret.getPropertyValue())) {
                    log.debug("Encrypted value " + xacmlSubscriberSecret.getPropertyValue());
                    String reEncryptedValue = symmetricReEncryption(xacmlSubscriberSecret.getPropertyValue(),
                            keyRotationConfig);
                    if (StringUtils.isEmpty(reEncryptedValue)) {
                        log.debug("Skipping since the value is already encrypted with new secret");
                        continue;
                    }
                    xacmlSubscriberSecret.setNewPropertyValue(reEncryptedValue);
                    log.debug("Re-encrypted value " + xacmlSubscriberSecret.getNewPropertyValue());
                    midChunkList.add(xacmlSubscriberSecret);
                }
            }
            IdentityDAO.getInstance()
                    .updateXACMLSecretChunks(midChunkList, keyRotationConfig, KeyRotationConstants.SUBSCRIBER_PASSWORD);
            startIndex = startIndex + keyRotationConfig.getChunkSize();
            chunkList = IdentityDAO.getInstance()
                    .getXACMLSecretChunks(startIndex, keyRotationConfig, KeyRotationConstants.SUBSCRIBER_PASSWORD);
        }
        log.debug("Finished re-encryption of the Identity secret data...");
    }
}

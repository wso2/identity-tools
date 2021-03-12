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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.keyrotation.config.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.dao.BPSProfileDAO;
import org.wso2.carbon.identity.keyrotation.dao.DBConstants;
import org.wso2.carbon.identity.keyrotation.dao.IdentityDAO;
import org.wso2.carbon.identity.keyrotation.dao.OAuthDAO;
import org.wso2.carbon.identity.keyrotation.dao.RegistryDAO;
import org.wso2.carbon.identity.keyrotation.dao.WorkFlowDAO;
import org.wso2.carbon.identity.keyrotation.model.BPSPassword;
import org.wso2.carbon.identity.keyrotation.model.OAuthCode;
import org.wso2.carbon.identity.keyrotation.model.OAuthSecret;
import org.wso2.carbon.identity.keyrotation.model.OAuthToken;
import org.wso2.carbon.identity.keyrotation.model.RegistryProperty;
import org.wso2.carbon.identity.keyrotation.model.TOTPSecret;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;
import org.wso2.carbon.identity.workflow.mgt.bean.RequestParameter;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;

import java.util.List;

import static org.wso2.carbon.identity.keyrotation.util.EncryptionUtil.reEncryptor;

/**
 * DB reEncryption service.
 */
public class DBKeyRotator {

    private static final Log log = LogFactory.getLog(DBKeyRotator.class);
    private static final DBKeyRotator instance = new DBKeyRotator();
    private static final String password = "password";
    private static final String privatekeyPass = "privatekeyPass";
    private static final String subscriberPassword = "subscriberPassword";

    public static DBKeyRotator getInstance() {

        return instance;
    }

    /**
     * ReEncryption of the DB data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public void dbReEncryptor(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.info("Re-encrypting DB data...");
        reEncryptIdentityTOTPData(keyRotationConfig);
        reEncryptOauthAuthData(keyRotationConfig);
        reEncryptOauthTokenData(keyRotationConfig);
        reEncryptOauthConsumerData(keyRotationConfig);
        reEncryptBPSData(keyRotationConfig);
        reEncryptWFRequestData(keyRotationConfig);
        reEncryptKeystorePasswordData(keyRotationConfig);
        reEncryptKeystorePrivatekeyPassData(keyRotationConfig);
        reEncryptSubscriberPasswordData(keyRotationConfig);
        reEncryptKerberosData(keyRotationConfig);
        log.info("Re-encrypting DB data completed...\n");
    }

    /**
     * ReEncryption of the IDN_IDENTITY_USER_DATA table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    private void reEncryptIdentityTOTPData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.info("Re-encryption of the Identity data...");
        int startIndex = 0;
        List<TOTPSecret> chunkList =
                IdentityDAO.getInstance().getTOTPSecretsChunks(startIndex, keyRotationConfig);
        while (CollectionUtils.isNotEmpty(chunkList)) {
            for (TOTPSecret totpSecret : chunkList) {
                if (totpSecret.getDataKey().equals(DBConstants.DATA_KEY)) {
                    log.info("Encrypted value " + totpSecret.getDataValue());
                    String reEncryptedValue = reEncryptor(totpSecret.getDataValue(), keyRotationConfig);
                    totpSecret.setDataValue(reEncryptedValue);
                    log.info("Re-encrypted value " + totpSecret.getDataValue());
                }
            }
            IdentityDAO.getInstance().updateTOTPSecretsChunks(chunkList, keyRotationConfig);
            startIndex = startIndex + DBConstants.CHUNK_SIZE;
            chunkList = IdentityDAO.getInstance().getTOTPSecretsChunks(startIndex, keyRotationConfig);
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
        while (CollectionUtils.isNotEmpty(chunkList)) {
            for (OAuthCode oAuthCode : chunkList) {
                //this condition is only for testing purposes
                if (oAuthCode.getConsumerKeyId().equals(DBConstants.TEST_CONSUMER_KEY_ID)) {
                    log.info("Encrypted value " + oAuthCode.getAuthorizationCode());
                    String reEncryptedValue = reEncryptor(oAuthCode.getAuthorizationCode(),
                            keyRotationConfig);
                    oAuthCode.setAuthorizationCode(reEncryptedValue);
                    log.info("Re-encrypted value " + oAuthCode.getAuthorizationCode());
                }
            }
            OAuthDAO.getInstance().updateOAuthCodeChunks(chunkList, keyRotationConfig);
            startIndex = startIndex + DBConstants.CHUNK_SIZE;
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
        List<OAuthToken> chunkList =
                OAuthDAO.getInstance().getOAuthTokenChunks(startIndex, keyRotationConfig);
        while (CollectionUtils.isNotEmpty(chunkList)) {
            for (OAuthToken oAuthToken : chunkList) {
                //this condition is only for testing purposes
                if (oAuthToken.getConsumerKeyId().equals(DBConstants.TEST_CONSUMER_KEY_ID)) {
                    log.info("Encrypted value " + oAuthToken.getAccessToken());
                    String reEncryptedValue = reEncryptor(oAuthToken.getAccessToken(),
                            keyRotationConfig);
                    oAuthToken.setAccessToken(reEncryptedValue);
                    log.info("Re-encrypted value " + oAuthToken.getAccessToken());
                }
            }
            OAuthDAO.getInstance().updateOAuthTokenChunks(chunkList, keyRotationConfig);
            startIndex = startIndex + DBConstants.CHUNK_SIZE;
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
        List<OAuthSecret> chunkList =
                OAuthDAO.getInstance().getOAuthSecretChunks(startIndex, keyRotationConfig);
        while (CollectionUtils.isNotEmpty(chunkList)) {
            for (OAuthSecret oAuthSecret : chunkList) {
                //this condition is only for testing purposes
                if (oAuthSecret.getAppName().equals(DBConstants.TEST_APP_NAME)) {
                    log.info("Encrypted value " + oAuthSecret.getConsumerSecret());
                    String reEncryptedValue = reEncryptor(oAuthSecret.getConsumerSecret(),
                            keyRotationConfig);
                    oAuthSecret.setConsumerSecret(reEncryptedValue);
                    log.info("Re-encrypted value " + oAuthSecret.getConsumerSecret());

                }
            }
            OAuthDAO.getInstance().updateOAuthSecretChunks(chunkList, keyRotationConfig);
            startIndex = startIndex + DBConstants.CHUNK_SIZE;
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
        int startIndex = 0;
        List<BPSPassword> chunkList =
                BPSProfileDAO.getInstance().getBpsPasswordChunks(startIndex, keyRotationConfig);
        while (CollectionUtils.isNotEmpty(chunkList)) {
            for (BPSPassword bpsPassword : chunkList) {
                //this condition is only for testing purposes
                if (bpsPassword.getUsername().equals(DBConstants.TEST_USERNAME)) {
                    log.info("Encrypted value " + bpsPassword.getPassword());
                    String reEncryptedValue = reEncryptor(bpsPassword.getPassword(), keyRotationConfig);
                    bpsPassword.setPassword(reEncryptedValue);
                    log.info("Re-encrypted value " + bpsPassword.getPassword());

                }

            }
            BPSProfileDAO.getInstance().updateBpsPasswordChunks(chunkList, keyRotationConfig);
            startIndex = startIndex + DBConstants.CHUNK_SIZE;
            chunkList = BPSProfileDAO.getInstance().getBpsPasswordChunks(startIndex, keyRotationConfig);
        }
    }

    /**
     * ReEncryption of the WF_REQUEST table data.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    private void reEncryptWFRequestData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.info("Re-encryption of the WF request data...");
        int startIndex = 0;
        List<WorkflowRequest> chunkList =
                WorkFlowDAO.getInstance().getWFRequestChunks(startIndex, keyRotationConfig);
        while (CollectionUtils.isNotEmpty(chunkList)) {
            for (WorkflowRequest wfRequest : chunkList) {
                for (RequestParameter parameter : wfRequest.getRequestParameters()) {
                    if (DBConstants.CREDENTIAL.equals(parameter.getName())) {
                        log.info("Encrypted value " + parameter.getValue().toString());
                        String reEncryptedValue = reEncryptor(parameter.getValue().toString(), keyRotationConfig);
                        parameter.setValue(reEncryptedValue);
                        log.info("Re-encrypted value " + parameter.getValue().toString());
                    }
                }
            }
            WorkFlowDAO.getInstance().updateWFRequestChunks(chunkList, keyRotationConfig);
            startIndex = startIndex + DBConstants.CHUNK_SIZE;
            chunkList = WorkFlowDAO.getInstance().getWFRequestChunks(startIndex, keyRotationConfig);
        }
    }

    /**
     * ReEncryption of keystore password in REG_PROPERTY table.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    private void reEncryptKeystorePasswordData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.info("Re-encryption of the keystore password property data...");
        int startIndex = 0;
        List<RegistryProperty> chunkList =
                RegistryDAO.getInstance().getRegPropertyDataChunks(startIndex, keyRotationConfig, password);
        while (CollectionUtils.isNotEmpty(chunkList)) {
            for (RegistryProperty regProperty : chunkList) {
                //this condition is only for testing purposes
                if (DBConstants.TEST_REG_TENANT_ID.equals(regProperty.getRegTenantId())) {
                    log.info("Encrypted value " + regProperty.getRegValue());
                    String reEncryptedValue = reEncryptor(regProperty.getRegValue(), keyRotationConfig);
                    regProperty.setRegValue(reEncryptedValue);
                    log.info("Re-encrypted value " + regProperty.getRegValue());
                }
            }
            RegistryDAO.getInstance().updateRegPropertyDataChunks(chunkList, keyRotationConfig, password);
            startIndex = startIndex + DBConstants.CHUNK_SIZE;
            chunkList = RegistryDAO.getInstance().getRegPropertyDataChunks(startIndex, keyRotationConfig, password);
        }
    }

    /**
     * ReEncryption of keystore privatekeyPass in REG_PROPERTY table.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    private void reEncryptKeystorePrivatekeyPassData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.info("Re-encryption of the keystore privatekeyPass property data...");
        int startIndex = 0;
        List<RegistryProperty> chunkList =
                RegistryDAO.getInstance().getRegPropertyDataChunks(startIndex, keyRotationConfig, privatekeyPass);
        while (CollectionUtils.isNotEmpty(chunkList)) {
            for (RegistryProperty regProperty : chunkList) {
                //this condition is only for testing purposes
                if (DBConstants.TEST_REG_TENANT_ID.equals(regProperty.getRegTenantId())) {
                    log.info("Encrypted value " + regProperty.getRegValue());
                    String reEncryptedValue = reEncryptor(regProperty.getRegValue(), keyRotationConfig);
                    regProperty.setRegValue(reEncryptedValue);
                    log.info("Re-encrypted value " + regProperty.getRegValue());
                }
            }
            RegistryDAO.getInstance().updateRegPropertyDataChunks(chunkList, keyRotationConfig, privatekeyPass);
            startIndex = startIndex + DBConstants.CHUNK_SIZE;
            chunkList =
                    RegistryDAO.getInstance().getRegPropertyDataChunks(startIndex, keyRotationConfig, privatekeyPass);
        }
    }

    /**
     * ReEncryption of subscriber password in REG_PROPERTY table.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    private void reEncryptSubscriberPasswordData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.info("Re-encryption of the subscriber password property data...");
        int startIndex = 0;
        List<RegistryProperty> chunkList =
                RegistryDAO.getInstance().getRegPropertyDataChunks(startIndex, keyRotationConfig, subscriberPassword);
        while (CollectionUtils.isNotEmpty(chunkList)) {
            for (RegistryProperty regProperty : chunkList) {
                if (DBConstants.TEST_REG_ID.equals(regProperty.getRegId())) {
                    log.info("Encrypted value " + regProperty.getRegValue());
                    String reEncryptedValue = reEncryptor(regProperty.getRegValue(), keyRotationConfig);
                    regProperty.setRegValue(reEncryptedValue);
                    log.info("Re-encrypted value " + regProperty.getRegValue());
                }
            }
            RegistryDAO.getInstance().updateRegPropertyDataChunks(chunkList, keyRotationConfig, subscriberPassword);
            startIndex = startIndex + DBConstants.CHUNK_SIZE;
            chunkList =
                    RegistryDAO.getInstance()
                            .getRegPropertyDataChunks(startIndex, keyRotationConfig, subscriberPassword);
        }
    }

    /**
     * ReEncryption of kerberos password in REG_PROPERTY table.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    private void reEncryptKerberosData(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        log.info("Re-encryption of the kerberos password property data...");
    }
}

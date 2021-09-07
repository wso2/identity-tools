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

DROP TABLE IDN_IDENTITY_USER_DATA_TEMP IF EXISTS;
CREATE TABLE IDN_IDENTITY_USER_DATA_TEMP (
                                             SYNC_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY
                                                 (START WITH 1 INCREMENT BY 1),
                                             TENANT_ID INTEGER DEFAULT -1234,
                                             USER_NAME VARCHAR(255) NOT NULL,
                                             DATA_KEY VARCHAR(255) NOT NULL,
                                             DATA_VALUE VARCHAR(2048),
                                             AVAILABILITY INTEGER NOT NULL,
                                             SYNCED INTEGER DEFAULT 0,
                                             PRIMARY KEY (SYNC_ID)
);

DELIMITER  //
CREATE OR REPLACE TRIGGER totp_sync_insert
AFTER INSERT ON IDN_IDENTITY_USER_DATA
REFERENCING NEW AS NEW
FOR EACH ROW
BEGIN
INSERT INTO IDN_IDENTITY_USER_DATA_TEMP (TENANT_ID, USER_NAME, DATA_KEY, DATA_VALUE, AVAILABILITY)
VALUES (NEW.TENANT_ID, NEW.USER_NAME, NEW.DATA_KEY, NEW.DATA_VALUE, 1);
END //
DELIMITER ;

DELIMITER  //
CREATE OR REPLACE TRIGGER totp_sync_update
AFTER UPDATE ON IDN_IDENTITY_USER_DATA
                                REFERENCING NEW AS NEW
                                FOR EACH ROW
BEGIN
INSERT INTO IDN_IDENTITY_USER_DATA_TEMP (TENANT_ID, USER_NAME, DATA_KEY, DATA_VALUE, AVAILABILITY)
VALUES (NEW.TENANT_ID, NEW.USER_NAME, NEW.DATA_KEY, NEW.DATA_VALUE, 1);
END //
DELIMITER ;

DELIMITER  //
CREATE OR REPLACE TRIGGER totp_sync_delete
AFTER DELETE ON IDN_IDENTITY_USER_DATA
REFERENCING OLD AS OLD
FOR EACH ROW
BEGIN
INSERT INTO IDN_IDENTITY_USER_DATA_TEMP (TENANT_ID, USER_NAME, DATA_KEY, DATA_VALUE, AVAILABILITY)
VALUES (OLD.TENANT_ID, OLD.USER_NAME, OLD.DATA_KEY, OLD.DATA_VALUE, 0);
END //
DELIMITER ;

DROP TABLE IDN_OAUTH2_AUTHORIZATION_CODE_TEMP IF EXISTS;
CREATE TABLE IDN_OAUTH2_AUTHORIZATION_CODE_TEMP (
                                                    SYNC_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY
                                                        (START WITH 1 INCREMENT BY 1),
                                                    CODE_ID VARCHAR (255),
                                                    AUTHORIZATION_CODE VARCHAR(2048),
                                                    CONSUMER_KEY_ID INTEGER,
                                                    CALLBACK_URL VARCHAR(2048),
                                                    SCOPE VARCHAR(2048),
                                                    AUTHZ_USER VARCHAR (100),
                                                    TENANT_ID INTEGER,
                                                    USER_DOMAIN VARCHAR(50),
                                                    TIME_CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                    VALIDITY_PERIOD BIGINT,
                                                    STATE VARCHAR (25) DEFAULT 'ACTIVE',
                                                    TOKEN_ID VARCHAR(255),
                                                    SUBJECT_IDENTIFIER VARCHAR(255),
                                                    PKCE_CODE_CHALLENGE VARCHAR(255),
                                                    PKCE_CODE_CHALLENGE_METHOD VARCHAR(128),
                                                    AUTHORIZATION_CODE_HASH VARCHAR(512),
                                                    IDP_ID INTEGER DEFAULT -1 NOT NULL,
                                                    AVAILABILITY INTEGER NOT NULL,
                                                    SYNCED INTEGER DEFAULT 0,
                                                    PRIMARY KEY (SYNC_ID)
);

DELIMITER  //
CREATE OR REPLACE TRIGGER oauth2_code_sync_insert
AFTER INSERT ON IDN_OAUTH2_AUTHORIZATION_CODE
REFERENCING NEW AS NEW
FOR EACH ROW
BEGIN
INSERT INTO IDN_OAUTH2_AUTHORIZATION_CODE_TEMP (CODE_ID, AUTHORIZATION_CODE, CONSUMER_KEY_ID, CALLBACK_URL, SCOPE,
                                                AUTHZ_USER, TENANT_ID, USER_DOMAIN, TIME_CREATED, VALIDITY_PERIOD,
                                                STATE, TOKEN_ID, SUBJECT_IDENTIFIER, PKCE_CODE_CHALLENGE,
                                                PKCE_CODE_CHALLENGE_METHOD, AUTHORIZATION_CODE_HASH, IDP_ID,
                                                AVAILABILITY) VALUES (NEW.CODE_ID, NEW.AUTHORIZATION_CODE,
                                                                      NEW.CONSUMER_KEY_ID, NEW.CALLBACK_URL, NEW.SCOPE,
                                                                      NEW.AUTHZ_USER, NEW.TENANT_ID, NEW.USER_DOMAIN,
                                                                      NEW.TIME_CREATED, NEW.VALIDITY_PERIOD, NEW.STATE,
                                                                      NEW.TOKEN_ID, NEW.SUBJECT_IDENTIFIER,
                                                                      NEW.PKCE_CODE_CHALLENGE,
                                                                      NEW.PKCE_CODE_CHALLENGE_METHOD,
                                                                      NEW.AUTHORIZATION_CODE_HASH, NEW.IDP_ID, 1);
END //
DELIMITER ;

DELIMITER  //
CREATE OR REPLACE TRIGGER oauth2_code_sync_update
AFTER UPDATE ON IDN_OAUTH2_AUTHORIZATION_CODE
                                REFERENCING NEW AS NEW
                                FOR EACH ROW
BEGIN
INSERT INTO IDN_OAUTH2_AUTHORIZATION_CODE_TEMP (CODE_ID, AUTHORIZATION_CODE, CONSUMER_KEY_ID, CALLBACK_URL, SCOPE,
                                                AUTHZ_USER, TENANT_ID, USER_DOMAIN, TIME_CREATED, VALIDITY_PERIOD,
                                                STATE, TOKEN_ID, SUBJECT_IDENTIFIER, PKCE_CODE_CHALLENGE,
                                                PKCE_CODE_CHALLENGE_METHOD, AUTHORIZATION_CODE_HASH, IDP_ID,
                                                AVAILABILITY) VALUES (NEW.CODE_ID, NEW.AUTHORIZATION_CODE,
                                                                      NEW.CONSUMER_KEY_ID, NEW.CALLBACK_URL, NEW.SCOPE,
                                                                      NEW.AUTHZ_USER, NEW.TENANT_ID, NEW.USER_DOMAIN,
                                                                      NEW.TIME_CREATED, NEW.VALIDITY_PERIOD, NEW.STATE,
                                                                      NEW.TOKEN_ID, NEW.SUBJECT_IDENTIFIER,
                                                                      NEW.PKCE_CODE_CHALLENGE,
                                                                      NEW.PKCE_CODE_CHALLENGE_METHOD,
                                                                      NEW.AUTHORIZATION_CODE_HASH, NEW.IDP_ID, 1);
END //
DELIMITER ;

DELIMITER  //
CREATE OR REPLACE TRIGGER oauth2_code_sync_delete
AFTER DELETE ON IDN_OAUTH2_AUTHORIZATION_CODE
REFERENCING OLD AS OLD
FOR EACH ROW
BEGIN
INSERT INTO IDN_OAUTH2_AUTHORIZATION_CODE_TEMP (CODE_ID, AUTHORIZATION_CODE, CONSUMER_KEY_ID, CALLBACK_URL, SCOPE,
                                                AUTHZ_USER, TENANT_ID, USER_DOMAIN, TIME_CREATED, VALIDITY_PERIOD,
                                                STATE, TOKEN_ID, SUBJECT_IDENTIFIER, PKCE_CODE_CHALLENGE,
                                                PKCE_CODE_CHALLENGE_METHOD, AUTHORIZATION_CODE_HASH, IDP_ID,
                                                AVAILABILITY) VALUES (OLD.CODE_ID, OLD.AUTHORIZATION_CODE,
                                                                      OLD.CONSUMER_KEY_ID, OLD.CALLBACK_URL,
                                                                      OLD.SCOPE, OLD.AUTHZ_USER, OLD.TENANT_ID,
                                                                      OLD.USER_DOMAIN, OLD.TIME_CREATED,
                                                                      OLD.VALIDITY_PERIOD, OLD.STATE, OLD.TOKEN_ID,
                                                                      OLD.SUBJECT_IDENTIFIER, OLD.PKCE_CODE_CHALLENGE,
                                                                      OLD.PKCE_CODE_CHALLENGE_METHOD,
                                                                      OLD.AUTHORIZATION_CODE_HASH, OLD.IDP_ID, 0);
END //
DELIMITER ;

DROP TABLE IDN_OAUTH2_ACCESS_TOKEN_TEMP IF EXISTS;
CREATE TABLE IDN_OAUTH2_ACCESS_TOKEN_TEMP (
                                              SYNC_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY
                                                  (START WITH 1 INCREMENT BY 1),
                                              TOKEN_ID VARCHAR (255),
                                              ACCESS_TOKEN VARCHAR(2048),
                                              REFRESH_TOKEN VARCHAR(2048),
                                              CONSUMER_KEY_ID INTEGER,
                                              AUTHZ_USER VARCHAR (100),
                                              TENANT_ID INTEGER,
                                              USER_DOMAIN VARCHAR(50),
                                              USER_TYPE VARCHAR (25),
                                              GRANT_TYPE VARCHAR (50),
                                              TIME_CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              REFRESH_TOKEN_TIME_CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              VALIDITY_PERIOD BIGINT,
                                              REFRESH_TOKEN_VALIDITY_PERIOD BIGINT,
                                              TOKEN_SCOPE_HASH VARCHAR(32),
                                              TOKEN_STATE VARCHAR(25) DEFAULT 'ACTIVE',
                                              TOKEN_STATE_ID VARCHAR (128) DEFAULT 'NONE',
                                              SUBJECT_IDENTIFIER VARCHAR(255),
                                              ACCESS_TOKEN_HASH VARCHAR(512),
                                              REFRESH_TOKEN_HASH VARCHAR(512),
                                              IDP_ID INTEGER DEFAULT -1 NOT NULL,
                                              TOKEN_BINDING_REF VARCHAR (32) DEFAULT 'NONE',
                                              AVAILABILITY INTEGER NOT NULL,
                                              SYNCED INTEGER DEFAULT 0,
                                              PRIMARY KEY (SYNC_ID)
);

DELIMITER  //
CREATE OR REPLACE TRIGGER token_sync_insert
AFTER INSERT ON IDN_OAUTH2_ACCESS_TOKEN
REFERENCING NEW AS NEW
FOR EACH ROW
BEGIN
INSERT INTO IDN_OAUTH2_ACCESS_TOKEN_TEMP (TOKEN_ID, ACCESS_TOKEN, REFRESH_TOKEN, CONSUMER_KEY_ID, AUTHZ_USER, TENANT_ID,
                                          USER_DOMAIN, USER_TYPE, GRANT_TYPE, TIME_CREATED, REFRESH_TOKEN_TIME_CREATED,
                                          VALIDITY_PERIOD, REFRESH_TOKEN_VALIDITY_PERIOD, TOKEN_SCOPE_HASH, TOKEN_STATE,
                                          TOKEN_STATE_ID, SUBJECT_IDENTIFIER, ACCESS_TOKEN_HASH, REFRESH_TOKEN_HASH,
                                          IDP_ID, TOKEN_BINDING_REF, AVAILABILITY)
                                          VALUES (NEW.TOKEN_ID, NEW.ACCESS_TOKEN, NEW.REFRESH_TOKEN, NEW.CONSUMER_KEY_ID,
                                                  NEW.AUTHZ_USER, NEW.TENANT_ID, NEW.USER_DOMAIN, NEW.USER_TYPE,
                                                  NEW.GRANT_TYPE, NEW.TIME_CREATED, NEW.REFRESH_TOKEN_TIME_CREATED,
                                                  NEW.VALIDITY_PERIOD, NEW.REFRESH_TOKEN_VALIDITY_PERIOD,
                                                  NEW.TOKEN_SCOPE_HASH, NEW.TOKEN_STATE, NEW.TOKEN_STATE_ID,
                                                  NEW.SUBJECT_IDENTIFIER, NEW.ACCESS_TOKEN_HASH, NEW.REFRESH_TOKEN_HASH,
                                                  NEW.IDP_ID, NEW.TOKEN_BINDING_REF, 1);
END //
DELIMITER ;

DELIMITER  //
CREATE OR REPLACE TRIGGER token_sync_update
AFTER UPDATE ON IDN_OAUTH2_ACCESS_TOKEN
                                REFERENCING NEW AS NEW
                                FOR EACH ROW
BEGIN
INSERT INTO IDN_OAUTH2_ACCESS_TOKEN_TEMP (TOKEN_ID, ACCESS_TOKEN, REFRESH_TOKEN, CONSUMER_KEY_ID, AUTHZ_USER, TENANT_ID,
                                          USER_DOMAIN, USER_TYPE, GRANT_TYPE, TIME_CREATED, REFRESH_TOKEN_TIME_CREATED,
                                          VALIDITY_PERIOD, REFRESH_TOKEN_VALIDITY_PERIOD, TOKEN_SCOPE_HASH, TOKEN_STATE,
                                          TOKEN_STATE_ID, SUBJECT_IDENTIFIER, ACCESS_TOKEN_HASH, REFRESH_TOKEN_HASH,
                                          IDP_ID, TOKEN_BINDING_REF, AVAILABILITY)
                                          VALUES (NEW.TOKEN_ID, NEW.ACCESS_TOKEN, NEW.REFRESH_TOKEN,
                                                  NEW.CONSUMER_KEY_ID, NEW.AUTHZ_USER, NEW.TENANT_ID, NEW.USER_DOMAIN,
                                                  NEW.USER_TYPE, NEW.GRANT_TYPE, NEW.TIME_CREATED,
                                                  NEW.REFRESH_TOKEN_TIME_CREATED, NEW.VALIDITY_PERIOD,
                                                  NEW.REFRESH_TOKEN_VALIDITY_PERIOD, NEW.TOKEN_SCOPE_HASH,
                                                  NEW.TOKEN_STATE, NEW.TOKEN_STATE_ID, NEW.SUBJECT_IDENTIFIER,
                                                  NEW.ACCESS_TOKEN_HASH, NEW.REFRESH_TOKEN_HASH, NEW.IDP_ID,
                                                  NEW.TOKEN_BINDING_REF, 1);
END //
DELIMITER ;

DELIMITER  //
CREATE OR REPLACE TRIGGER token_sync_delete
AFTER DELETE ON IDN_OAUTH2_ACCESS_TOKEN
REFERENCING OLD AS OLD
FOR EACH ROW
BEGIN
INSERT INTO IDN_OAUTH2_ACCESS_TOKEN_TEMP (TOKEN_ID, ACCESS_TOKEN, REFRESH_TOKEN, CONSUMER_KEY_ID, AUTHZ_USER, TENANT_ID,
                                          USER_DOMAIN, USER_TYPE, GRANT_TYPE, TIME_CREATED, REFRESH_TOKEN_TIME_CREATED,
                                          VALIDITY_PERIOD, REFRESH_TOKEN_VALIDITY_PERIOD, TOKEN_SCOPE_HASH, TOKEN_STATE,
                                          TOKEN_STATE_ID, SUBJECT_IDENTIFIER, ACCESS_TOKEN_HASH, REFRESH_TOKEN_HASH,
                                          IDP_ID, TOKEN_BINDING_REF, AVAILABILITY)
                                          VALUES (OLD.TOKEN_ID, OLD.ACCESS_TOKEN, OLD.REFRESH_TOKEN, OLD.CONSUMER_KEY_ID,
                                                  OLD.AUTHZ_USER, OLD.TENANT_ID, OLD.USER_DOMAIN, OLD.USER_TYPE,
                                                  OLD.GRANT_TYPE, OLD.TIME_CREATED, OLD.REFRESH_TOKEN_TIME_CREATED,
                                                  OLD.VALIDITY_PERIOD, OLD.REFRESH_TOKEN_VALIDITY_PERIOD,
                                                  OLD.TOKEN_SCOPE_HASH, OLD.TOKEN_STATE, OLD.TOKEN_STATE_ID,
                                                  OLD.SUBJECT_IDENTIFIER, OLD.ACCESS_TOKEN_HASH, OLD.REFRESH_TOKEN_HASH,
                                                  OLD.IDP_ID, OLD.TOKEN_BINDING_REF, 0);
END //
DELIMITER ;

DROP TABLE IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP IF EXISTS;
CREATE TABLE IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP (
                                                    SYNC_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY
                                                        (START WITH 1 INCREMENT BY 1),
                                                    TOKEN_ID VARCHAR (255),
                                                    TOKEN_SCOPE VARCHAR (60),
                                                    TENANT_ID INTEGER DEFAULT -1,
                                                    AVAILABILITY INTEGER NOT NULL,
                                                    SYNCED INTEGER DEFAULT 0,
                                                    PRIMARY KEY (SYNC_ID)
);

DELIMITER  //
CREATE OR REPLACE TRIGGER scope_sync_insert
AFTER INSERT ON IDN_OAUTH2_ACCESS_TOKEN_SCOPE
REFERENCING NEW AS NEW
FOR EACH ROW
BEGIN
INSERT INTO IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP (TOKEN_ID, TOKEN_SCOPE, TENANT_ID, AVAILABILITY)
VALUES (NEW.TOKEN_ID, NEW.TOKEN_SCOPE, NEW.TENANT_ID, 1);
END //
DELIMITER ;

DELIMITER  //
CREATE OR REPLACE TRIGGER scope_sync_update
AFTER UPDATE ON IDN_OAUTH2_ACCESS_TOKEN_SCOPE
                                REFERENCING NEW AS NEW
                                FOR EACH ROW
BEGIN
INSERT INTO IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP (TOKEN_ID, TOKEN_SCOPE, TENANT_ID, AVAILABILITY)
VALUES (NEW.TOKEN_ID, NEW.TOKEN_SCOPE, NEW.TENANT_ID, 1);
END //
DELIMITER ;

DELIMITER  //
CREATE OR REPLACE TRIGGER scope_sync_delete
AFTER DELETE ON IDN_OAUTH2_ACCESS_TOKEN_SCOPE
REFERENCING OLD AS OLD
FOR EACH ROW
BEGIN
INSERT INTO IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP (TOKEN_ID, TOKEN_SCOPE, TENANT_ID, AVAILABILITY)
VALUES (OLD.TOKEN_ID, OLD.TOKEN_SCOPE, OLD.TENANT_ID, 0);
END //
DELIMITER ;
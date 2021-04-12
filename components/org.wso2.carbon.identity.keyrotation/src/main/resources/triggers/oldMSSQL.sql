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

DROP TABLE IF EXISTS IDN_IDENTITY_USER_DATA_TEMP;

CREATE TABLE IDN_IDENTITY_USER_DATA_TEMP (
                                             SYNC_ID INTEGER NOT NULL IDENTITY(1,1),
                                             TENANT_ID INTEGER DEFAULT -1234,
                                             USER_NAME VARCHAR(255) NOT NULL,
                                             DATA_KEY VARCHAR(255) NOT NULL,
                                             DATA_VALUE VARCHAR(2048),
                                             AVAILABILITY INTEGER NOT NULL,
                                             SYNCED INTEGER DEFAULT 0,
                                             PRIMARY KEY (SYNC_ID)
);

DROP TRIGGER IF EXISTS totp_sync_insert;
CREATE TRIGGER totp_sync_insert
    ON IDN_IDENTITY_USER_DATA
    AFTER INSERT
AS
BEGIN
INSERT INTO IDN_IDENTITY_USER_DATA_TEMP (TENANT_ID, USER_NAME, DATA_KEY, DATA_VALUE, AVAILABILITY)
SELECT i.TENANT_ID, i.USER_NAME, i.DATA_KEY, i.DATA_VALUE, 1 FROM Inserted AS i
END;

DROP TRIGGER IF EXISTS totp_sync_update;
CREATE TRIGGER totp_sync_update
    ON IDN_IDENTITY_USER_DATA
    AFTER UPDATE
              AS
BEGIN
INSERT INTO IDN_IDENTITY_USER_DATA_TEMP (TENANT_ID, USER_NAME, DATA_KEY, DATA_VALUE, AVAILABILITY)
SELECT i.TENANT_ID, i.USER_NAME, i.DATA_KEY, i.DATA_VALUE, 1 FROM Inserted AS i
END;

DROP TRIGGER IF EXISTS totp_sync_delete;
CREATE TRIGGER totp_sync_delete
    ON IDN_IDENTITY_USER_DATA
    AFTER DELETE
AS
BEGIN
INSERT INTO IDN_IDENTITY_USER_DATA_TEMP (TENANT_ID, USER_NAME, DATA_KEY, DATA_VALUE, AVAILABILITY)
SELECT d.TENANT_ID, d.USER_NAME, d.DATA_KEY, d.DATA_VALUE, 0 FROM Deleted AS d
END;

DROP TABLE IF EXISTS IDN_OAUTH2_AUTHORIZATION_CODE_TEMP;

CREATE TABLE IDN_OAUTH2_AUTHORIZATION_CODE_TEMP (
                                                    SYNC_ID INTEGER NOT NULL IDENTITY(1,1),
                                                    CODE_ID VARCHAR (255),
                                                    AUTHORIZATION_CODE VARCHAR(2048),
                                                    CONSUMER_KEY_ID INTEGER,
                                                    CALLBACK_URL VARCHAR(2048),
                                                    SCOPE VARCHAR(2048),
                                                    AUTHZ_USER VARCHAR (100),
                                                    TENANT_ID INTEGER,
                                                    USER_DOMAIN VARCHAR(50),
                                                    TIME_CREATED DATETIME,
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

DROP TRIGGER IF EXISTS oauth2_code_sync_insert;
CREATE TRIGGER oauth2_code_sync_insert
    ON IDN_OAUTH2_AUTHORIZATION_CODE
    AFTER INSERT
AS
BEGIN
INSERT INTO IDN_OAUTH2_AUTHORIZATION_CODE_TEMP (CODE_ID, AUTHORIZATION_CODE, CONSUMER_KEY_ID, CALLBACK_URL, SCOPE,
                                                AUTHZ_USER, TENANT_ID, USER_DOMAIN, TIME_CREATED, VALIDITY_PERIOD,
                                                STATE, TOKEN_ID, SUBJECT_IDENTIFIER, PKCE_CODE_CHALLENGE,
                                                PKCE_CODE_CHALLENGE_METHOD, AUTHORIZATION_CODE_HASH, IDP_ID,
                                                AVAILABILITY) SELECT i.CODE_ID, i.AUTHORIZATION_CODE, i.CONSUMER_KEY_ID,
                                                                     i.CALLBACK_URL, i.SCOPE, i.AUTHZ_USER, i.TENANT_ID,
                                                                     i.USER_DOMAIN, i.TIME_CREATED, i.VALIDITY_PERIOD,
                                                                     i.STATE, i.TOKEN_ID, i.SUBJECT_IDENTIFIER,
                                                                     i.PKCE_CODE_CHALLENGE, i.PKCE_CODE_CHALLENGE_METHOD,
                                                                     i.AUTHORIZATION_CODE_HASH, i.IDP_ID, 1
FROM Inserted AS i
END;

DROP TRIGGER IF EXISTS oauth2_code_sync_update;
CREATE TRIGGER oauth2_code_sync_update
    ON IDN_OAUTH2_AUTHORIZATION_CODE
    AFTER UPDATE
              AS
BEGIN
INSERT INTO IDN_OAUTH2_AUTHORIZATION_CODE_TEMP (CODE_ID, AUTHORIZATION_CODE, CONSUMER_KEY_ID, CALLBACK_URL, SCOPE,
                                                AUTHZ_USER, TENANT_ID, USER_DOMAIN, TIME_CREATED, VALIDITY_PERIOD,
                                                STATE, TOKEN_ID, SUBJECT_IDENTIFIER, PKCE_CODE_CHALLENGE,
                                                PKCE_CODE_CHALLENGE_METHOD, AUTHORIZATION_CODE_HASH, IDP_ID,
                                                AVAILABILITY) SELECT i.CODE_ID, i.AUTHORIZATION_CODE, i.CONSUMER_KEY_ID,
                                                                     i.CALLBACK_URL, i.SCOPE, i.AUTHZ_USER, i.TENANT_ID,
                                                                     i.USER_DOMAIN, i.TIME_CREATED, i.VALIDITY_PERIOD,
                                                                     i.STATE, i.TOKEN_ID, i.SUBJECT_IDENTIFIER,
                                                                     i.PKCE_CODE_CHALLENGE, i.PKCE_CODE_CHALLENGE_METHOD,
                                                                     i.AUTHORIZATION_CODE_HASH, i.IDP_ID, 1
FROM Inserted AS i
END;

DROP TRIGGER IF EXISTS oauth2_code_sync_delete;
CREATE TRIGGER oauth2_code_sync_delete
    ON IDN_OAUTH2_AUTHORIZATION_CODE
    AFTER DELETE
AS
BEGIN
INSERT INTO IDN_OAUTH2_AUTHORIZATION_CODE_TEMP (CODE_ID, AUTHORIZATION_CODE, CONSUMER_KEY_ID, CALLBACK_URL, SCOPE,
                                                AUTHZ_USER, TENANT_ID, USER_DOMAIN, TIME_CREATED, VALIDITY_PERIOD,
                                                STATE, TOKEN_ID, SUBJECT_IDENTIFIER, PKCE_CODE_CHALLENGE,
                                                PKCE_CODE_CHALLENGE_METHOD, AUTHORIZATION_CODE_HASH, IDP_ID,
                                                AVAILABILITY) SELECT d.CODE_ID, d.AUTHORIZATION_CODE, d.CONSUMER_KEY_ID,
                                                                     d.CALLBACK_URL, d.SCOPE, d.AUTHZ_USER, d.TENANT_ID,
                                                                     d.USER_DOMAIN, d.TIME_CREATED, d.VALIDITY_PERIOD,
                                                                     d.STATE, d.TOKEN_ID, d.SUBJECT_IDENTIFIER,
                                                                     d.PKCE_CODE_CHALLENGE, d.PKCE_CODE_CHALLENGE_METHOD,
                                                                     d.AUTHORIZATION_CODE_HASH, d.IDP_ID, 0
FROM Deleted AS d
END;

DROP TABLE IF EXISTS IDN_OAUTH2_ACCESS_TOKEN_TEMP;

CREATE TABLE IDN_OAUTH2_ACCESS_TOKEN_TEMP (
                                              SYNC_ID INTEGER NOT NULL IDENTITY(1,1),
                                              TOKEN_ID VARCHAR (255),
                                              ACCESS_TOKEN VARCHAR(2048),
                                              REFRESH_TOKEN VARCHAR(2048),
                                              CONSUMER_KEY_ID INTEGER,
                                              AUTHZ_USER VARCHAR (100),
                                              TENANT_ID INTEGER,
                                              USER_DOMAIN VARCHAR(50),
                                              USER_TYPE VARCHAR (25),
                                              GRANT_TYPE VARCHAR (50),
                                              TIME_CREATED DATETIME,
                                              REFRESH_TOKEN_TIME_CREATED DATETIME,
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

DROP TRIGGER IF EXISTS token_sync_insert;
CREATE TRIGGER token_sync_insert
    ON IDN_OAUTH2_ACCESS_TOKEN
    AFTER INSERT
AS
BEGIN
INSERT INTO IDN_OAUTH2_ACCESS_TOKEN_TEMP (TOKEN_ID, ACCESS_TOKEN, REFRESH_TOKEN, CONSUMER_KEY_ID, AUTHZ_USER, TENANT_ID,
                                          USER_DOMAIN, USER_TYPE, GRANT_TYPE, TIME_CREATED, REFRESH_TOKEN_TIME_CREATED,
                                          VALIDITY_PERIOD, REFRESH_TOKEN_VALIDITY_PERIOD, TOKEN_SCOPE_HASH, TOKEN_STATE,
                                          TOKEN_STATE_ID, SUBJECT_IDENTIFIER, ACCESS_TOKEN_HASH, REFRESH_TOKEN_HASH,
                                          IDP_ID, TOKEN_BINDING_REF, AVAILABILITY)
                                          SELECT i.TOKEN_ID, i.ACCESS_TOKEN, i.REFRESH_TOKEN, i.CONSUMER_KEY_ID,
                                                 i.AUTHZ_USER, i.TENANT_ID, i.USER_DOMAIN, i.USER_TYPE, i.GRANT_TYPE,
                                                 i.TIME_CREATED, i.REFRESH_TOKEN_TIME_CREATED, i.VALIDITY_PERIOD,
                                                 i.REFRESH_TOKEN_VALIDITY_PERIOD, i.TOKEN_SCOPE_HASH, i.TOKEN_STATE,
                                                 i.TOKEN_STATE_ID, i.SUBJECT_IDENTIFIER, i.ACCESS_TOKEN_HASH,
                                                 i.REFRESH_TOKEN_HASH, i.IDP_ID, i.TOKEN_BINDING_REF, 1
FROM Inserted AS i
END;

DROP TRIGGER IF EXISTS token_sync_update;
CREATE TRIGGER token_sync_update
    ON IDN_OAUTH2_ACCESS_TOKEN
    AFTER UPDATE
              AS
BEGIN
INSERT INTO IDN_OAUTH2_ACCESS_TOKEN_TEMP (TOKEN_ID, ACCESS_TOKEN, REFRESH_TOKEN, CONSUMER_KEY_ID, AUTHZ_USER, TENANT_ID,
                                          USER_DOMAIN, USER_TYPE, GRANT_TYPE, TIME_CREATED, REFRESH_TOKEN_TIME_CREATED,
                                          VALIDITY_PERIOD, REFRESH_TOKEN_VALIDITY_PERIOD, TOKEN_SCOPE_HASH, TOKEN_STATE,
                                          TOKEN_STATE_ID, SUBJECT_IDENTIFIER, ACCESS_TOKEN_HASH, REFRESH_TOKEN_HASH,
                                          IDP_ID, TOKEN_BINDING_REF, AVAILABILITY)
                                          SELECT i.TOKEN_ID, i.ACCESS_TOKEN, i.REFRESH_TOKEN, i.CONSUMER_KEY_ID,
                                                 i.AUTHZ_USER, i.TENANT_ID, i.USER_DOMAIN, i.USER_TYPE, i.GRANT_TYPE,
                                                 i.TIME_CREATED, i.REFRESH_TOKEN_TIME_CREATED, i.VALIDITY_PERIOD,
                                                 i.REFRESH_TOKEN_VALIDITY_PERIOD, i.TOKEN_SCOPE_HASH, i.TOKEN_STATE,
                                                 i.TOKEN_STATE_ID, i.SUBJECT_IDENTIFIER, i.ACCESS_TOKEN_HASH,
                                                 i.REFRESH_TOKEN_HASH, i.IDP_ID, i.TOKEN_BINDING_REF, 1
FROM Inserted AS i
END;

DROP TRIGGER IF EXISTS token_sync_delete;
CREATE TRIGGER token_sync_delete
    ON IDN_OAUTH2_ACCESS_TOKEN
    AFTER DELETE
AS
BEGIN
INSERT INTO IDN_OAUTH2_ACCESS_TOKEN_TEMP (TOKEN_ID, ACCESS_TOKEN, REFRESH_TOKEN, CONSUMER_KEY_ID, AUTHZ_USER, TENANT_ID,
                                          USER_DOMAIN, USER_TYPE, GRANT_TYPE, TIME_CREATED, REFRESH_TOKEN_TIME_CREATED,
                                          VALIDITY_PERIOD, REFRESH_TOKEN_VALIDITY_PERIOD, TOKEN_SCOPE_HASH, TOKEN_STATE,
                                          TOKEN_STATE_ID, SUBJECT_IDENTIFIER, ACCESS_TOKEN_HASH, REFRESH_TOKEN_HASH,
                                          IDP_ID, TOKEN_BINDING_REF, AVAILABILITY)
                                          SELECT d.TOKEN_ID, d.ACCESS_TOKEN, d.REFRESH_TOKEN, d.CONSUMER_KEY_ID,
                                                 d.AUTHZ_USER, d.TENANT_ID, d.USER_DOMAIN, d.USER_TYPE, d.GRANT_TYPE,
                                                 d.TIME_CREATED, d.REFRESH_TOKEN_TIME_CREATED, d.VALIDITY_PERIOD,
                                                 d.REFRESH_TOKEN_VALIDITY_PERIOD, d.TOKEN_SCOPE_HASH, d.TOKEN_STATE,
                                                 d.TOKEN_STATE_ID, d.SUBJECT_IDENTIFIER, d.ACCESS_TOKEN_HASH,
                                                 d.REFRESH_TOKEN_HASH, d.IDP_ID, d.TOKEN_BINDING_REF, 0
FROM Deleted AS d
END;

DROP TABLE IF EXISTS IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP;

CREATE TABLE IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP (
                                                    SYNC_ID INTEGER NOT NULL IDENTITY(1,1),
                                                    TOKEN_ID VARCHAR (255),
                                                    TOKEN_SCOPE VARCHAR (60),
                                                    TENANT_ID INTEGER DEFAULT -1,
                                                    AVAILABILITY INTEGER NOT NULL,
                                                    SYNCED INTEGER DEFAULT 0,
                                                    PRIMARY KEY (SYNC_ID)
);

DROP TRIGGER IF EXISTS scope_sync_insert;
CREATE TRIGGER scope_sync_insert
    ON IDN_OAUTH2_ACCESS_TOKEN_SCOPE
    AFTER INSERT
AS
BEGIN
INSERT INTO IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP (TOKEN_ID, TOKEN_SCOPE, TENANT_ID, AVAILABILITY)
SELECT i.TOKEN_ID, i.TOKEN_SCOPE, i.TENANT_ID, 1 FROM Inserted AS i
END;

DROP TRIGGER IF EXISTS scope_sync_update;
CREATE TRIGGER scope_sync_update
    ON IDN_OAUTH2_ACCESS_TOKEN_SCOPE
    AFTER UPDATE
              AS
BEGIN
INSERT INTO IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP (TOKEN_ID, TOKEN_SCOPE, TENANT_ID, AVAILABILITY)
SELECT i.TOKEN_ID, i.TOKEN_SCOPE, i.TENANT_ID, 1 FROM Inserted AS i
END;

DROP TRIGGER IF EXISTS scope_sync_delete;
CREATE TRIGGER scope_sync_delete
    ON IDN_OAUTH2_ACCESS_TOKEN_SCOPE
    AFTER DELETE
AS
BEGIN
INSERT INTO IDN_OAUTH2_ACCESS_TOKEN_SCOPE_TEMP (TOKEN_ID, TOKEN_SCOPE, TENANT_ID, AVAILABILITY)
SELECT d.TOKEN_ID, d.TOKEN_SCOPE, d.TENANT_ID, 0 FROM Deleted AS d
END;
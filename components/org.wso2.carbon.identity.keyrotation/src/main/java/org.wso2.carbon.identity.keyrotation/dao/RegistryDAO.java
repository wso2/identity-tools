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
import org.wso2.carbon.identity.keyrotation.model.RegistryProperty;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to re-encrypt keystore properties within REG_PROPERTY in DB.
 */
public class RegistryDAO {

    private static final Logger log = Logger.getLogger(RegistryDAO.class);
    private static final RegistryDAO instance = new RegistryDAO();
    private static final String REG_ID = "REG_ID";
    private static final String REG_NAME = "REG_NAME";
    private static final String REG_VALUE = "REG_VALUE";
    private static final String REG_TENANT_ID = "REG_TENANT_ID";
    public static int updateCount = 0;
    public static int failedCount = 0;

    public RegistryDAO() {

    }

    public static RegistryDAO getInstance() {

        return instance;
    }

    /**
     * To retrieve the keystore data in REG_PROPERTY as chunks.
     *
     * @param startIndex        The start index of the data chunk.
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @return List comprising of the records in the table.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public List<RegistryProperty> getRegPropertyDataChunks(int startIndex, KeyRotationConfig keyRotationConfig,
                                                           String property) throws KeyRotationException {

        List<RegistryProperty> regPropertyList = new ArrayList<>();
        String query = DBConstants.GET_REG_PROPERTY_DATA;
        int firstIndex = startIndex;
        int secIndex = DBConstants.CHUNK_SIZE;
        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getNewRegDBUrl(), keyRotationConfig.getNewRegUsername(),
                        keyRotationConfig.getNewRegPassword())) {
            if (connection.getMetaData().getDriverName().contains(DBConstants.POSTGRESQL)) {
                query = DBConstants.GET_REG_PROPERTY_DATA_POSTGRE;
                firstIndex = DBConstants.CHUNK_SIZE;
                secIndex = startIndex;
            } else if (connection.getMetaData().getDriverName().contains(DBConstants.MSSQL) ||
                    connection.getMetaData().getDriverName().contains(DBConstants.ORACLE)) {
                query = DBConstants.GET_REG_PROPERTY_DATA_OTHER;
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, property);
                preparedStatement.setInt(2, firstIndex);
                preparedStatement.setInt(3, secIndex);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    regPropertyList
                            .add(new RegistryProperty(resultSet.getString(REG_ID),
                                    resultSet.getString(REG_NAME),
                                    resultSet.getString(REG_VALUE),
                                    resultSet.getString(REG_TENANT_ID)));
                }
            } catch (SQLException e) {
                throw new KeyRotationException("Error while retrieving registry property " + property + " from " +
                        "REG_PROPERTY.", e);
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new registry DB.", e);
        }
        return regPropertyList;
    }

    /**
     * To reEncrypt the registry property value in REG_PROPERTY using the new key.
     *
     * @param updateRegPropertyList The list containing records that should be re-encrypted.
     * @param keyRotationConfig     Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown if something unexpected happens during key rotation.
     */
    public void updateRegPropertyDataChunks(List<RegistryProperty> updateRegPropertyList,
                                            KeyRotationConfig keyRotationConfig, String property)
            throws KeyRotationException {

        try (Connection connection = DriverManager
                .getConnection(keyRotationConfig.getNewRegDBUrl(), keyRotationConfig.getNewRegUsername(),
                        keyRotationConfig.getNewRegPassword())) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(DBConstants.UPDATE_REG_PROPERTY_DATA)) {
                for (RegistryProperty regProperty : updateRegPropertyList) {
                    preparedStatement.setString(1, regProperty.getRegValue());
                    preparedStatement.setInt(2, Integer.parseInt(regProperty.getRegId()));
                    preparedStatement.setInt(3, Integer.parseInt(regProperty.getRegTenantId()));
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
                updateCount += updateRegPropertyList.size();
            } catch (SQLException e) {
                connection.rollback();
                RegistryProperty faulty = updateRegPropertyList.get(0);
                log.error(
                        "Error while updating registry property: " + property + " from REG_PROPERTY, trying the chunk" +
                                " row by row again. ", e);
                PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.UPDATE_REG_PROPERTY_DATA);
                for (RegistryProperty regProperty : updateRegPropertyList) {
                    try {
                        faulty = regProperty;
                        preparedStatement.setString(1, regProperty.getRegValue());
                        preparedStatement.setInt(2, Integer.parseInt(regProperty.getRegId()));
                        preparedStatement.setInt(3, Integer.parseInt(regProperty.getRegTenantId()));
                        preparedStatement.executeUpdate();
                        connection.commit();
                        updateCount++;
                    } catch (SQLException err) {
                        connection.rollback();
                        log.error("Error while updating registry property: " + property + " from REG_PROPERTY of " +
                                "record with reg id: " + faulty.getRegId() + " reg tenant id: " +
                                faulty.getRegTenantId() + " ," + err);
                        failedCount++;
                    }
                }
            }
        } catch (SQLException e) {
            throw new KeyRotationException("Error while connecting to new registry DB.", e);
        }
    }
}

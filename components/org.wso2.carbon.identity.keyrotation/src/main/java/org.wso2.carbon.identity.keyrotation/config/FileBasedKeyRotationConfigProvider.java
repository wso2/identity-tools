package org.wso2.carbon.identity.keyrotation.config;

import org.wso2.carbon.identity.keyrotation.config.model.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A file-based provider implementation for the {@link KeyRotationConfig} model.
 * It is an implementation of the base class, {@link KeyRotationConfigProvider}.
 */
public class FileBasedKeyRotationConfigProvider implements KeyRotationConfigProvider {

    private KeyRotationConfig keyRotationConfig = new KeyRotationConfig();

    public FileBasedKeyRotationConfigProvider(InputStream fileInputStream) throws KeyRotationException {

        Properties properties = new Properties();
        try {
            properties.load(fileInputStream);
        } catch (IOException e) {
            throw new KeyRotationException("Error occurred while loading the yaml file: ", e);
        }
        initConfig(properties);
    }

    private void initConfig(Properties properties) throws KeyRotationException {

        KeyRotationConfigValidator configValidator = KeyRotationConfigValidator.getInstance();

        String oldSecretKey = properties.getProperty(KeyRotationConstants.OLD_SECRET_KEY);
        String newSecretKey = properties.getProperty(KeyRotationConstants.NEW_SECRET_KEY);
        String newISHome = properties.getProperty(KeyRotationConstants.NEW_IS_HOME);
        String oldIdnDBUrl = properties.getProperty(KeyRotationConstants.OLD_IDN_DB_URL);
        String oldIdnUsername = properties.getProperty(KeyRotationConstants.OLD_IDN_USERNAME);
        String oldIdnPassword = properties.getProperty(KeyRotationConstants.OLD_IDN_PASSWORD);
        String newIdnDBUrl = properties.getProperty(KeyRotationConstants.NEW_IDN_DB_URL);
        String newIdnUsername = properties.getProperty(KeyRotationConstants.NEW_IDN_USERNAME);
        String newIdnPassword = properties.getProperty(KeyRotationConstants.NEW_IDN_PASSWORD);
        String newRegDBUrl = properties.getProperty(KeyRotationConstants.NEW_REG_DB_URL);
        String newRegUsername = properties.getProperty(KeyRotationConstants.NEW_REG_USERNAME);
        String newRegPassword = properties.getProperty(KeyRotationConstants.NEW_REG_PASSWORD);
        String enableDBMigrator = properties.getProperty(KeyRotationConstants.ENABLE_DB_MIGRATOR);
        String enableConfigMigrator = properties.getProperty(KeyRotationConstants.ENABLE_CONFIG_MIGRATOR);
        String enableSyncMigrator = properties.getProperty(KeyRotationConstants.ENABLE_SYNC_MIGRATOR);

        configValidator.validateFilePath(KeyRotationConstants.NEW_IS_HOME, newISHome);
        configValidator.validateURI(KeyRotationConstants.OLD_IDN_DB_URL, oldIdnDBUrl);
        configValidator.validateURI(KeyRotationConstants.NEW_IDN_DB_URL, newIdnDBUrl);
        configValidator.validateURI(KeyRotationConstants.NEW_REG_DB_URL, newRegDBUrl);
        configValidator.validateBoolean(KeyRotationConstants.ENABLE_DB_MIGRATOR, enableDBMigrator);
        configValidator.validateBoolean(KeyRotationConstants.ENABLE_CONFIG_MIGRATOR, enableConfigMigrator);
        configValidator.validateBoolean(KeyRotationConstants.ENABLE_SYNC_MIGRATOR, enableSyncMigrator);

        keyRotationConfig.setOldSecretKey(oldSecretKey);
        keyRotationConfig.setNewSecretKey(newSecretKey);
        keyRotationConfig.setNewISHome(newISHome);
        keyRotationConfig.setOldIdnDBUrl(oldIdnDBUrl);
        keyRotationConfig.setOldIdnUsername(oldIdnUsername);
        keyRotationConfig.setOldIdnPassword(oldIdnPassword);
        keyRotationConfig.setNewIdnDBUrl(newIdnDBUrl);
        keyRotationConfig.setNewIdnUsername(newIdnUsername);
        keyRotationConfig.setNewIdnPassword(newIdnPassword);
        keyRotationConfig.setNewRegDBUrl(newRegDBUrl);
        keyRotationConfig.setNewRegUsername(newRegUsername);
        keyRotationConfig.setNewRegPassword(newRegPassword);
        keyRotationConfig.setEnableDBMigrator(Boolean.parseBoolean(enableDBMigrator));
        keyRotationConfig.setEnableConfigMigrator(Boolean.parseBoolean(enableConfigMigrator));
        keyRotationConfig.setEnableSyncMigrator(Boolean.parseBoolean(enableSyncMigrator));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeyRotationConfig getKeyRotationConfig() {

        return keyRotationConfig;
    }
}

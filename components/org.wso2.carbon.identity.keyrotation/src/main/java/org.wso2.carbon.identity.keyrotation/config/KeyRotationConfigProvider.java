package org.wso2.carbon.identity.keyrotation.config;

import org.wso2.carbon.identity.keyrotation.config.model.KeyRotationConfig;

/**
 * A "provider" interface for the {@link KeyRotationConfig} model.
 *
 * @see FileBasedKeyRotationConfigProvider
 */
public interface KeyRotationConfigProvider {

    /**
     * Retrieves the {@code KeyRotationConfig} object with the relevant configurations.
     *
     * @return {@link KeyRotationConfig} Object containing the relevant configurations.
     */
    KeyRotationConfig getKeyRotationConfig();

}

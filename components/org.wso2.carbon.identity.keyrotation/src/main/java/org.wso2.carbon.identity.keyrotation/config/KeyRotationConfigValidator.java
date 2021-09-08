package org.wso2.carbon.identity.keyrotation.config;

import org.apache.commons.lang3.BooleanUtils;
import org.wso2.carbon.identity.keyrotation.config.model.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationConstants;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;

public class KeyRotationConfigValidator {

    public static final KeyRotationConfigValidator instance = new KeyRotationConfigValidator();

    public static KeyRotationConfigValidator getInstance() {

        return instance;
    }

    /**
     * To check whether the loaded configurations are valid and not null.
     *
     * @param keyRotationConfig Configuration data needed to perform the task.
     * @throws KeyRotationException Exception thrown while checking for null values in the loaded properties.yaml file.
     */
    private void checkKeyRotationConfigs(KeyRotationConfig keyRotationConfig) throws KeyRotationException {

        Field[] props = KeyRotationConfig.getInstance().getClass().getDeclaredFields();
        try {
            for (int i = 0; i < props.length - 2; i++) {
                if (props[i].get(keyRotationConfig) == null) {
                    throw new KeyRotationException(
                            "Error occurred, null value found in property, " + props[i].getName());
                }
                if (KeyRotationConstants.NEW_IS_HOME.equals(props[i].getName())) {
                    File file = new File(props[i].get(keyRotationConfig).toString());
                    if (!file.exists()) {
                        throw new KeyRotationException(
                                "Error occurred while finding " + props[i].getName() + " path.");
                    }
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new KeyRotationException("Error occurred while checking for null values in the loaded properties " +
                    "file, ", e);
        }
    }

    public void validateURI(String name, String value) throws KeyRotationException {

        try {
            new URI(value);
        } catch (URISyntaxException e) {
            String message = String.format("invalid URI: %s for %s", value, name);
            throw new KeyRotationException(message, e);
        }
    }

    public void validateBoolean(String name, String value) throws KeyRotationException {

        Boolean bool = BooleanUtils.toBooleanObject(value);
        if (bool == null) {
            String message = String.format("invalid boolean value: %s for %s", value, name);
            throw new KeyRotationException(message);
        }
    }

    public void validateFilePath(String name, String value) throws KeyRotationException {

        File file = new File(value);
        if (!file.exists()) {
            String message = String.format("file/directory path: %s for %s does not exist", value, name);
            throw new KeyRotationException(message);
        }
    }

}

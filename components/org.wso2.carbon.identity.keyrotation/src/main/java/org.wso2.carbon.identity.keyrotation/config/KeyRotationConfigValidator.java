package org.wso2.carbon.identity.keyrotation.config;

import org.apache.commons.lang3.BooleanUtils;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Validator for configuration properties retrieved by the {@link KeyRotationConfigProvider}.
 */
public class KeyRotationConfigValidator {

    private static final KeyRotationConfigValidator instance = new KeyRotationConfigValidator();

    public static KeyRotationConfigValidator getInstance() {

        return instance;
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

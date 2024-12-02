package org.wso2.carbon.identity.keyrotation.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.wso2.carbon.identity.keyrotation.config.model.KeyRotationConfig;
import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

public class ConfigFileKeyRotatorTest {

    private static final Logger logger = LoggerFactory.getLogger(ConfigFileKeyRotatorTest.class);

    static KeyRotationConfig keyRotationConfig;
    private static final String NEW_SECRET = "b988c775c435f05c3da1fbecfe76c1e7";
    private static final String OLD_SECRET = "9501cb661d04b0dbe68d17bff2060737";

    @BeforeAll
    protected static void init() {
        keyRotationConfig = new KeyRotationConfig();
        keyRotationConfig.setISHome("/Users/indeewariwijesiri/Documents/Servers/1.0.4/identity-tools/components" +
                "/org.wso2.carbon.identity.keyrotation/src/test/resources/wso2is");
        keyRotationConfig.setOldSecretKey(OLD_SECRET);
        keyRotationConfig.setNewSecretKey(NEW_SECRET);
    }

    @Test
    public void testConfigFileReEncryptor() {
        try {
            ConfigFileKeyRotator.getInstance().configFileReEncryptor(keyRotationConfig);
            Assert.assertTrue(true);
        } catch (KeyRotationException e) {
            logger.error("Error while testing ConfigFileKeyRotator");
            Assert.fail();
        }
    }

}

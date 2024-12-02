package org.wso2.carbon.identity.keyrotation.model;

/**
 * This class is the data holder of the secret values in IDN_SECRET table
 */
public class IdentitySecret {

    private final String id;
    private final String secretValue;
    private String newSecretValue;

    public IdentitySecret(String id, String secretValue) {

        this.id = id;
        this.secretValue = secretValue;
    }

    public String getId() {

        return id;
    }

    public String getSecretValue() {

        return secretValue;
    }

    public String getNewSecretValue() {

        return newSecretValue;
    }

    public void setNewSecretValue(String newSecretValue) {

        this.newSecretValue = newSecretValue;
    }
}

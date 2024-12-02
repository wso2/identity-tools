package org.wso2.carbon.identity.keyrotation.model;

/**
 * Data holder for the IDN_XACML_SUBSCRIBER_PROPERTY secrets.
 */
public class XACMLSubscriberSecret {

    private final String subscriberId;
    private final String tenantId;
    private final String propertyKey;
    private final String propertyValue;
    private String newPropertyValue;

    /**
     * Constructs a new {@code XACMLSubscriberSecret} with the specified subscriber details and property values.
     *
     * @param subscriberId  The unique identifier of the subscriber.
     * @param tenantId      The tenant associated with the subscriber.
     * @param propertyKey   The key of the encrypted property.
     * @param propertyValue The encrypted property
     */
    public XACMLSubscriberSecret(String subscriberId, String tenantId, String propertyKey, String propertyValue) {

        this.subscriberId = subscriberId;
        this.tenantId = tenantId;
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;
    }

    /**
     * Retrieves the subscriber ID.
     *
     * @return The subscriber ID.
     */
    public String getSubscriberId() {

        return subscriberId;
    }

    /**
     * Retrieves the tenant ID.
     *
     * @return The tenant ID.
     */
    public String getTenantId() {

        return tenantId;
    }

    /**
     * Retrieves the property key.
     *
     * @return The encrypted property key.
     */
    public String getPropertyKey() {

        return propertyKey;
    }

    /**
     * Retrieves the encrypted property value.
     *
     * @return The encrypted property value.
     */
    public String getPropertyValue() {

        return propertyValue;
    }

    /**
     * Retrieves the new encrypted property value, if set.
     *
     * @return The new encrypted property value, or {@code null} if not set.
     */
    public String getNewPropertyValue() {

        return newPropertyValue;
    }

    /**
     * Sets a new encrypted property value.
     *
     * @param newPropertyValue The new encrypted property value to set.
     */
    public void setNewPropertyValue(String newPropertyValue) {

        this.newPropertyValue = newPropertyValue;
    }
}

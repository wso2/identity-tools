package org.wso2.carbon.identity.keyrotation.model;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.wso2.carbon.identity.keyrotation.util.EncryptionUtil.checkPlainText;

/**
 * The data holder for the WF_REQUEST secrets.
 */
public class WorkFlowRequestSecret {

    private final String requestSecret;
    private String newRequestSecret;
    private final WorkflowRequest workflowRequest;
    private WorkflowRequest newWorkflowRequest;
    private final String updatedTime;
    private final byte[]  serializedWorkFlowRequest;

    public WorkFlowRequestSecret(byte[] serializedData, String updatedTime) {

        if (serializedData != null) {
            this.serializedWorkFlowRequest = serializedData;
            this.workflowRequest = deserializeWFRequest(serializedData);
            this.requestSecret = this.workflowRequest.getRequestParameters().stream()
                    .filter(parameter -> WorkflowRequest.CREDENTIAL.equalsIgnoreCase(parameter.getName()))
                    .map(parameter -> (String) parameter.getValue())
                    .findFirst()
                    .orElse(null);
        } else {
            this.serializedWorkFlowRequest = null;
            this.workflowRequest = null;
            this.requestSecret = null;
        }
        this.updatedTime = updatedTime;
    }

    public WorkflowRequest getWorkflowRequest() {

        return workflowRequest;
    }

    public void setNewWorkflowRequest(WorkflowRequest newWorkflowRequest) {

        this.newWorkflowRequest = newWorkflowRequest;
    }

    public String getUpdatedTime() {

        return updatedTime;
    }

    public String getRequestSecret() {

        return requestSecret;
    }

    public String getNewRequestSecret() {

        return newRequestSecret;
    }

    public void setNewRequestSecret(String newRequestSecret) {

        this.newRequestSecret = newRequestSecret;
        setNewWorkflowRequest(null);
    }

    public byte[] getSerializedWorkFlowRequest() {

        return serializedWorkFlowRequest;
    }

    /**
     * To convert the stored byte stream to a WorkFlowRequest object.
     *
     * @param serializedData The byte stream.
     * @return WorkFlowRequest object.
     */
    private WorkflowRequest deserializeWFRequest(byte[] serializedData) {

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedData);
        ObjectInputStream objectInputStream;
        try {
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Object objectRead;
        try {
            objectRead = objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return (WorkflowRequest) objectRead;
    }

    /**
     * To convert the passed WorkFlowRequest object to a byte stream.
     *
     * @return The byte stream.
     */
    public byte[] serializedReEncryptedWorkFlow() {

        updatedNewRequest();
        if (this.workflowRequest != null && StringUtils.isNotBlank(this.requestSecret) &&
                !checkPlainText(this.newRequestSecret) && this.newWorkflowRequest != null) {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(this.newWorkflowRequest);
                objectOutputStream.close();
                return byteArrayOutputStream.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private void updatedNewRequest() {

        if (workflowRequest != null && newWorkflowRequest == null) {
            WorkflowRequest workflowRequestClone;
            try {
                workflowRequestClone = this.workflowRequest.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            if (workflowRequestClone != null && StringUtils.isNotBlank(this.newRequestSecret) &&
                    !checkPlainText(this.newRequestSecret)) {
                workflowRequestClone.getRequestParameters().stream()
                        .filter(parameter -> WorkflowRequest.CREDENTIAL.equalsIgnoreCase(parameter.getName()))
                        .findFirst()
                        .ifPresent(parameter -> parameter.setValue(this.newRequestSecret));
                this.newWorkflowRequest = workflowRequestClone;
            }
        }
    }
}

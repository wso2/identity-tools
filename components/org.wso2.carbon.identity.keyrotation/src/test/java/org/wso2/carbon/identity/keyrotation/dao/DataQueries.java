package org.wso2.carbon.identity.keyrotation.dao;

import org.wso2.carbon.identity.keyrotation.util.KeyRotationException;

public interface DataQueries {

    void setDatabaseQueries();

    void setChunkSize();

    void performAssertions() throws KeyRotationException;

}

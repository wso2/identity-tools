package org.wso2.carbon.identity.keyrotation.util;

public class CryptoException extends Exception{

    public CryptoException() {

        super();
    }

    public CryptoException(String message) {

        super(message);
    }

    public CryptoException(String message, Throwable e) {

        super(message, e);
    }

}

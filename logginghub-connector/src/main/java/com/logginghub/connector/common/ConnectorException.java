package com.logginghub.connector.common;

public class ConnectorException extends Exception {

    public ConnectorException() {
        super();
    }

    public ConnectorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectorException(String message) {
        super(message);
    }

    public ConnectorException(Throwable cause) {
        super(cause);
    }

}

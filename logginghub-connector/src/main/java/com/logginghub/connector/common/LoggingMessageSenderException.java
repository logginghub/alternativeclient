package com.logginghub.connector.common;

public class LoggingMessageSenderException extends Exception {

    public LoggingMessageSenderException() {
        super();
    }

    public LoggingMessageSenderException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoggingMessageSenderException(String message) {
        super(message);
    }

    public LoggingMessageSenderException(Throwable cause) {
        super(cause);
    }

}

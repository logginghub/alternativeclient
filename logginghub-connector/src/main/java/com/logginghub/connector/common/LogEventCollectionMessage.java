package com.logginghub.connector.common;

public class LogEventCollectionMessage implements LoggingMessage {
    private LogEventCollection logEventCollection;

    public LogEventCollectionMessage(LogEventCollection event) {
        logEventCollection = event;
    }

    public LogEventCollection getLogEventCollection() {
        return logEventCollection;
    }

    @Override public String toString() {
        return "[LogEventCollection size=" + logEventCollection.size() + "]";
    }
}

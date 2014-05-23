package com.logginghub.connector.common;

public interface LoggingMessageSource {
    public void addLoggingMessageListener(LoggingMessageListener listener);

    public void removeLoggingMessageListener(LoggingMessageListener listener);
}

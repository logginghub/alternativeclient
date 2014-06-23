package com.logginghub.connector.common;

/**
 * Listener that allows a class to receive new logging messages from LoggingMessageSources.
 * 
 * @author admin
 */
public interface LoggingMessageListener {
    public void onNewLoggingMessage(LoggingMessage message);
}

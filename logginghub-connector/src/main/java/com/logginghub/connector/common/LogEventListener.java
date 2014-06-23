package com.logginghub.connector.common;

/**
 * Listener that allows a class to receive new log events from LogEventSources.
 * 
 * @author admin
 */
public interface LogEventListener {
    public void onNewLogEvent(LogEvent event);
}

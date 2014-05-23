package com.logginghub.connector.common;


/**
 * Interface for classes that act as sources of log events, allowing adding and removing of log
 * event listeners.
 * 
 * @author admin
 */
public interface LogEventSource {    
    public void addLogEventListener(LogEventListener listener);
    public void removeLogEventListener(LogEventListener listener);
}

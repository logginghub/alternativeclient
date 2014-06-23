package com.logginghub.connector.common;

/**
 * Spectacular hack - at the moment there is no other way to take a value out of the socket appender
 * and into other code. Needs to be destroyed on some sort of sacrificial pyre as soon as we work
 * out a better way to join the various bits together...
 * 
 * @author James
 */
public class GlobalLoggingParameters {

    private static final GlobalLoggingParameters singleton = new GlobalLoggingParameters();
    
    private String applicationName;
    private int pid;
    private String destination;

    private GlobalLoggingParameters() {

    }

    public static final GlobalLoggingParameters getInstance() {
        return singleton;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public int getPid() {
        return pid;
    }

    public String getDestination() {
        return destination;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }
}

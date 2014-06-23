package com.logginghub.utils;


public class VLPorts {

    public static final String socketHubProperty = "vl_socketHubDefaultPort";
    public static final String socketHubUPDProperty = "vl_socketHubUDPPort";
    public static final String restfulListenerProperty = "vl_restfulListenerPort";
    public static final String containerProperty = "vl_containerPort";
    public final static String socketTextReader1Property = "vl_socketTextReader1DefaultPort";

    public static int getSocketHubDefaultPort() {
        return EnvironmentProperties.getInteger(socketHubProperty, 58770);
    }

    public static int getSocketHubUDPPort() {
        return EnvironmentProperties.getInteger(socketHubUPDProperty, 58770);
    }
    
    public static int getSocketTextReader1DefaultPort() {
        return EnvironmentProperties.getInteger(socketTextReader1Property, 58772);
    }

    public static int getRestfulListenerPort() {
        return EnvironmentProperties.getInteger(restfulListenerProperty, 58779);
    }

    public static int getContainerDefaultPort() {
        return EnvironmentProperties.getInteger(containerProperty, 59000);         
    }
}

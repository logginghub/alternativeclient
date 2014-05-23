package com.logginghub.utils;


public class VLPorts {

    public static final String socketHubProperty = "vl_socketHubDefaultPort";
    public static final String socketHubUPDProperty = "vl_socketHubUDPPort";
    public static final String restfulListenerProperty = "vl_restfulListenerPort";
    public static final String containerProperty = "vl_containerPort";
    public final static String telemetryProperty = "vl_telemetryHubDefaultPort";
    public final static String telemetryMessaging3Property = "vl_telemetryMessaging3HubDefaultPort";
    public final static String socketTextReader1Property = "vl_socketTextReader1DefaultPort";
    public static final String repositoryDefaultPort = "vl_vlptRepositoryDefaultPort";
    public static final String repositoryWebDefaultPort = "vl_vlptRepositoryDefaultWebPort";

    public static final String vlptAgent1Port = "vl_vlptAgent1Port";
    public static final String vlptAgent2Port = "vl_vlptAgent2Port";
    public static final String vlptAgent3Port = "vl_vlptAgent3Port";
    public static final String vlptAgent4Port = "vl_vlptAgent4Port";
    public static final String vlptAgent5Port = "vl_vlptAgent5Port";

    public static int getVLPTAgent1Port() {
        return EnvironmentProperties.getInteger(vlptAgent1Port, 20001);
    }

    public static int getVLPTAgent2Port() {
        return EnvironmentProperties.getInteger(vlptAgent2Port, 20002);
    }

    public static int getVLPTAgent3Port() {
        return EnvironmentProperties.getInteger(vlptAgent3Port, 20003);
    }

    public static int getVLPTAgent4Port() {
        return EnvironmentProperties.getInteger(vlptAgent4Port, 20004);
    }

    public static int getVLPTAgent5Port() {
        return EnvironmentProperties.getInteger(vlptAgent5Port, 20005);
    }

    public static int getRepositoryDefaultPort() {
        return EnvironmentProperties.getInteger(repositoryDefaultPort, 58775);
    }

    public static int getRepositoryWebDefaultPort() {
        return EnvironmentProperties.getInteger(repositoryWebDefaultPort, 8082);
    }

    public static int getSocketHubDefaultPort() {
        return EnvironmentProperties.getInteger(socketHubProperty, 58770);
    }

    public static int getSocketHubUDPPort() {
        return EnvironmentProperties.getInteger(socketHubUPDProperty, 58770);
    }
    
    public static int getTelemetryHubDefaultPort() {
        return EnvironmentProperties.getInteger(telemetryProperty, 58771);
    }

    public static int getTelemetryMessaging3HubDefaultPort() {
        return EnvironmentProperties.getInteger(telemetryMessaging3Property, 58778);
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

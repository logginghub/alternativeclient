package com.logginghub.connector.common;

public interface SocketConnectorListener {
    void onConnectionEstablished();
    void onConnectionLost(String reason);
}

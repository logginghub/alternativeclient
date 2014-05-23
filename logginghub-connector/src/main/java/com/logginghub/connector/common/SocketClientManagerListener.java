package com.logginghub.connector.common;

public interface SocketClientManagerListener
{
    void onStateChanged(SocketClientManager.State fromState, SocketClientManager.State toState);
}

package com.logginghub.connector.common;


public interface LoggingMessageSender {
    void send(LoggingMessage message) throws LoggingMessageSenderException;
}

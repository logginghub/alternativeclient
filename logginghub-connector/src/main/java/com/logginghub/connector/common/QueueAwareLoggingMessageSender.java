package com.logginghub.connector.common;

public interface QueueAwareLoggingMessageSender extends LoggingMessageSender {
    boolean isSendQueueEmpty();
}

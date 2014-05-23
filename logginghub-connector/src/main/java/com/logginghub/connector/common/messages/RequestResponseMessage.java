package com.logginghub.connector.common.messages;

import com.logginghub.connector.common.LoggingMessage;

public interface RequestResponseMessage extends LoggingMessage {
    int getRequestID();
    void setRequestID(int requestID);
}

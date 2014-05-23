package com.logginghub.connector.common.messages;

public class BaseRequestResponseMessage implements RequestResponseMessage {
    private int requestID;

    public BaseRequestResponseMessage() {
    }
    
    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    public int getRequestID() {
        return requestID;
    }

}

package com.logginghub.connector.common.messages;

import java.io.Serializable;

import com.logginghub.connector.common.LoggingMessage;

public class SubscriptionResponseMessage implements LoggingMessage,Serializable
{
    public SubscriptionResponseMessage()
    {
    }

    @Override public String toString()
    {
        return "[SubscriptionResponseMessage]";
    }
}

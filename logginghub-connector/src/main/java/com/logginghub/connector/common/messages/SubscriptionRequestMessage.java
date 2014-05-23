package com.logginghub.connector.common.messages;

import java.io.Serializable;

import com.logginghub.connector.common.LoggingMessage;

public class SubscriptionRequestMessage implements LoggingMessage, Serializable
{
    public SubscriptionRequestMessage()
    {
    }

    @Override
    public String toString()
    {
        return "[SubscriptionRequestMessage]";
    }
}

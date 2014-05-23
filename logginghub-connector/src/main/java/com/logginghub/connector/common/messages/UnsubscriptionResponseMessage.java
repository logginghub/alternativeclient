package com.logginghub.connector.common.messages;

import java.io.Serializable;

import com.logginghub.connector.common.LoggingMessage;

public class UnsubscriptionResponseMessage implements LoggingMessage, Serializable
{
    public UnsubscriptionResponseMessage()
    {
        
    }

    private static final long serialVersionUID = 1L;

    @Override
    public String toString()
    {
        return "[UnsubscriptionResponseMessage]";
    }
}

package com.logginghub.connector.common.messages;

import java.io.Serializable;

import com.logginghub.connector.common.LoggingMessage;

public class UnsubscriptionRequestMessage implements LoggingMessage, Serializable
{
    private static final long serialVersionUID = 1L;
    
    public UnsubscriptionRequestMessage()
    {
    }

    @Override
    public String toString()
    {
        return "[UnsubscriptionRequestMessage]";
    }
}

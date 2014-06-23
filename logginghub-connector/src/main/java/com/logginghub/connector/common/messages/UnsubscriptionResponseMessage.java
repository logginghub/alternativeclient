package com.logginghub.connector.common.messages;

import com.logginghub.connector.common.LoggingMessage;
import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofReader;
import com.logginghub.sof.SofWriter;

public class UnsubscriptionResponseMessage implements LoggingMessage, SerialisableObject {
    public UnsubscriptionResponseMessage() {

    }

    @Override public String toString() {
        return "[UnsubscriptionResponseMessage]";
    }

    public void read(SofReader reader) throws SofException {}

    public void write(SofWriter writer) throws SofException {}
}

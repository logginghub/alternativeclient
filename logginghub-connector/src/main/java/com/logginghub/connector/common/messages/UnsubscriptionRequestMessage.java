package com.logginghub.connector.common.messages;

import com.logginghub.connector.common.LoggingMessage;
import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofReader;
import com.logginghub.sof.SofWriter;

public class UnsubscriptionRequestMessage implements LoggingMessage, SerialisableObject {

    public UnsubscriptionRequestMessage() {}

    @Override public String toString() {
        return "[UnsubscriptionRequestMessage]";
    }

    public void read(SofReader reader) throws SofException {}

    public void write(SofWriter writer) throws SofException {}
}

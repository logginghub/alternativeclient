package com.logginghub.connector.common.messages;

import com.logginghub.connector.common.DefaultLogEvent;
import com.logginghub.connector.common.LogEvent;
import com.logginghub.connector.common.LoggingMessage;
import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofReader;
import com.logginghub.sof.SofWriter;

public class LogEventMessage implements LoggingMessage, SerialisableObject {
    private LogEvent logEvent;

    public LogEventMessage(LogEvent event) {
        logEvent = event;
    }

    public LogEvent getLogEvent() {
        return logEvent;
    }

    @Override public String toString() {
        return "[LogEventMessage message='" + logEvent.getMessage() + "']";
    }

    public void read(SofReader reader) throws SofException {
        this.logEvent = (DefaultLogEvent) reader.readObject(1);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, (DefaultLogEvent) logEvent);
    }

}

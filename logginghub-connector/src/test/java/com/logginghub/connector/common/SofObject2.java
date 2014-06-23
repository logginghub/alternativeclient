package com.logginghub.connector.common;

import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofReader;
import com.logginghub.sof.SofWriter;

public class SofObject2 implements SerialisableObject, LoggingMessage {

    private String message;
    
    public SofObject2() {}

    public SofObject2(String message) {
        this.message = message;
    }

    public void read(SofReader reader) throws SofException {
        message = reader.readString(1);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, message);
    }

    public String getMessage() {
        return message;
    }
}

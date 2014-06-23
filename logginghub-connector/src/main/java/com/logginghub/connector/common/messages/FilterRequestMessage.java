package com.logginghub.connector.common.messages;

import com.logginghub.connector.common.LoggingMessage;
import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofReader;
import com.logginghub.sof.SofWriter;

public class FilterRequestMessage implements LoggingMessage, SerialisableObject {
    private int levelFilter;

    public FilterRequestMessage() {}
    
    public FilterRequestMessage(int levelFilter) {
        this.levelFilter = levelFilter;
    }
    
    public int getLevelFilter() {
        return levelFilter;
    }

    @Override public String toString() {
        return "FilterRequestMessage [levelFilter=" + levelFilter + "]";
    }

    public void read(SofReader reader) throws SofException {
        levelFilter = reader.readInt(0);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(0, levelFilter);
    }
}

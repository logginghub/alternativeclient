package com.logginghub.connector.common.messages;

import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofReader;
import com.logginghub.sof.SofWriter;

public class HistoricalIndexResponse extends BaseRequestResponseMessage implements SerialisableObject {

    private HistoricalIndexElement[] elements;
    private boolean lastBatch;

    public HistoricalIndexElement[] getElements() {
        return elements;
    }

    public void setElements(HistoricalIndexElement[] elements) {
        this.elements = elements;
    }

    public void read(SofReader reader) throws SofException {
        this.elements = (HistoricalIndexElement[]) reader.readObjectArray(1, HistoricalIndexElement.class);
        setRequestID(reader.readInt(2));
        this.lastBatch = reader.readBoolean(3);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, elements, HistoricalIndexElement.class);
        writer.write(2, getRequestID());
        writer.write(3, lastBatch);
    }

    @Override public String toString() {
        return "HistoricalIndexResponse [elements=" + (elements != null ? elements.length : "<null>") + "]";
    }

    public void setLastBatch(boolean lastBatch) {
        this.lastBatch = lastBatch;
    }
    
    public boolean isLastBatch() {
        return lastBatch;
    }

}

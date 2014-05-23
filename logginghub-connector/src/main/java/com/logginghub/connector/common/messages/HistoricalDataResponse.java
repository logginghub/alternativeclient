package com.logginghub.connector.common.messages;

import com.logginghub.connector.common.DefaultLogEvent;
import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofReader;
import com.logginghub.sof.SofWriter;

public class HistoricalDataResponse extends BaseRequestResponseMessage implements SerialisableObject {

    private DefaultLogEvent[] events;

    private CompressedBlock<DefaultLogEvent> compressedBlock = new CompressedBlock<DefaultLogEvent>();

    private boolean lastBatch;

    public HistoricalDataResponse() {}

    public DefaultLogEvent[] getEvents() {
        return events;
    }

    public void setEvents(DefaultLogEvent[] events) {
        this.events = events;
    }

    public void read(SofReader reader) throws SofException {
        setRequestID(reader.readInt(1));
        this.compressedBlock = (CompressedBlock<DefaultLogEvent>) reader.readObject(2);
        this.events = compressedBlock.decodeAll(DefaultLogEvent.class);
        this.lastBatch = reader.readBoolean(3);
    }

    public void write(SofWriter writer) throws SofException {
        compressedBlock.clear();
        compressedBlock.addAll(events);
        writer.write(1, getRequestID());
        writer.write(2, compressedBlock);
        writer.write(3, lastBatch);
        
    }

    public void setLastBatch(boolean lastBatch) {
        this.lastBatch = lastBatch;
    }

    public boolean isLastBatch() {
        return lastBatch;
    }
}

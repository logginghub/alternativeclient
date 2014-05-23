package com.logginghub.connector.common.messages;

import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofReader;
import com.logginghub.sof.SofWriter;

public class HistoricalDataRequest extends BaseRequestResponseMessage implements SerialisableObject {

    private long start;
    private long end;
    private int levelFilter;
    private String quickfilter;
    private boolean mostRecentFirst = true;

    public HistoricalDataRequest() {}

    public HistoricalDataRequest(long start, long end) {
        super();
        this.start = start;
        this.end = end;
    }

    public void setMostRecentFirst(boolean mostRecentFirst) {
        this.mostRecentFirst = mostRecentFirst;
    }
    
    public boolean isMostRecentFirst() {
        return mostRecentFirst;
    }
    
    public void setLevelFilter(int levelFilter) {
        this.levelFilter = levelFilter;
    }

    public int getLevelFilter() {
        return levelFilter;
    }

    public String getQuickfilter() {
        return quickfilter;
    }

    public void setQuickfilter(String quickfilter) {
        this.quickfilter = quickfilter;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public long getStart() {
        return start;
    }

    public void read(SofReader reader) throws SofException {
        setRequestID(reader.readInt(1));
        this.start = reader.readLong(2);
        this.end = reader.readLong(3);
        this.levelFilter = reader.readInt(4);
        this.quickfilter = reader.readString(5);
        this.mostRecentFirst = reader.readBoolean(6);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, getRequestID());
        writer.write(2, start);
        writer.write(3, end);
        writer.write(4, levelFilter);
        writer.write(5, quickfilter);
        writer.write(6, mostRecentFirst);
    }

}

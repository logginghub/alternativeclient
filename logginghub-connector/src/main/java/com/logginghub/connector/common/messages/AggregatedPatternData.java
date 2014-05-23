package com.logginghub.connector.common.messages;

import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofReader;
import com.logginghub.sof.SofWriter;
import com.logginghub.utils.Logger;
import com.logginghub.utils.TimeUtils;

public class AggregatedPatternData implements SerialisableObject {

    private double value;
    private long time;
    private String series;
    private long interval;

    private AggregationKey key;

    public AggregatedPatternData() {}

    public AggregatedPatternData(double value, long time, String series, long interval, AggregationKey key) {
        super();
        this.value = value;
        this.time = time;
        this.series = series;
        this.interval = interval;
        this.key = key;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getSeries() {
        return series;
    }

    public long getInterval() {
        return interval;
    }

    public long getTime() {
        return time;
    }

    public double getValue() {
        return value;
    }

    public AggregationKey getKey() {
        return key;
    }

    public void setKey(AggregationKey key) {
        this.key = key;
    }

    @Override public String toString() {
        return "AggregatedPatternData [series=" +
               series +
               ", interval=" +
               TimeUtils.formatIntervalMillisecondsCompact(interval) +
               ", time=" +
               Logger.toDateString(time) +
               ", value=" +
               value +
               "]";
    }

    public void read(SofReader reader) throws SofException {
        value = reader.readDouble(1);
        time = reader.readLong(2);
        series = reader.readString(3);
        interval = reader.readLong(4);
        key = (AggregationKey) reader.readObject(5);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, value);
        writer.write(2, time);
        writer.write(3, series);
        writer.write(4, interval);
        writer.write(5, key);
    }

}

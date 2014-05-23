package com.logginghub.connector.common.messages;

import java.util.Arrays;

import com.logginghub.connector.common.Channels;
import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofReader;
import com.logginghub.sof.SofWriter;
import com.logginghub.utils.TimeUtils;

public class AggregationKey implements SerialisableObject {

    private String patternName;
    private String label;
    private AggregationType aggregationType;
    private long interval;
    private String[] eventParts;

    public AggregationKey() {}

    public AggregationKey(String patternName, String label, AggregationType aggregationType, long interval, String[] eventParts) {
        super();
        this.patternName = patternName;
        this.label = label;
        this.aggregationType = aggregationType;
        this.interval = interval;
        this.eventParts = eventParts;
    }

    public String getPatternName() {
        return patternName;
    }

    public void setPatternName(String patternName) {
        this.patternName = patternName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public AggregationType getType() {
        return aggregationType;
    }

    public void setType(AggregationType aggregationType) {
        this.aggregationType = aggregationType;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public String[] getEventParts() {
        return eventParts;
    }

    public void setEventParts(String[] eventParts) {
        this.eventParts = eventParts;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aggregationType == null) ? 0 : aggregationType.hashCode());
        result = prime * result + Arrays.hashCode(eventParts);
        result = prime * result + (int) (interval ^ (interval >>> 32));
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + ((patternName == null) ? 0 : patternName.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AggregationKey other = (AggregationKey) obj;
        if (aggregationType != other.aggregationType) {
            return false;
        }
        if (!Arrays.equals(eventParts, other.eventParts)) {
            return false;
        }
        if (interval != other.interval) {
            return false;
        }
        if (label == null) {
            if (other.label != null) {
                return false;
            }
        }
        else if (!label.equals(other.label)) {
            return false;
        }
        if (patternName == null) {
            if (other.patternName != null) {
                return false;
            }
        }
        else if (!patternName.equals(other.patternName)) {
            return false;
        }
        return true;
    }

    public void read(SofReader reader) throws SofException {
        patternName = reader.readString(1);
        label = reader.readString(2);
        aggregationType = AggregationType.valueOf(reader.readString(3));
        interval = reader.readLong(4);
        eventParts = reader.readStringArray(5);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, patternName);
        writer.write(2, label);
        writer.write(3, aggregationType.toString());
        writer.write(4, interval);
        writer.write(5, eventParts);
    }

    @Override public String toString() {
        return "AggregationKey [patternName=" +
               patternName +
               ", label=" +
               label +
               ", aggregationType=" +
               aggregationType +
               ", interval=" +
               interval +
               ", eventParts=" +
               Arrays.toString(eventParts) +
               "]";
    }

    public String getChannel() {
        StringBuilder builder = new StringBuilder();        
        builder.append(Channels.patternisedEventUpdates).append(Channels.divider);
        builder.append(patternName).append(Channels.divider);
        builder.append(label).append(Channels.divider);
        builder.append(aggregationType.name()).append(Channels.divider);
        builder.append(TimeUtils.formatIntervalMillisecondsCompact(interval)).append(Channels.divider);

        // TODO : it might be necessary to put the event parts higher up the key to stop more general subscriptions getting the detail results?
        if (eventParts != null) {
            for (String eventPart : eventParts) {
                builder.append(eventPart).append(Channels.divider);
            }
        }
        
        return builder.toString();
    }

}

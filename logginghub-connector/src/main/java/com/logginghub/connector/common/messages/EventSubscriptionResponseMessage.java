package com.logginghub.connector.common.messages;

import java.util.Arrays;

import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofReader;
import com.logginghub.sof.SofWriter;

public class EventSubscriptionResponseMessage extends BaseRequestResponseMessage implements SerialisableObject {

    private String[] channels;
    private boolean subscribe;
    private boolean success;
    private String reason;

    public EventSubscriptionResponseMessage(int requestID, boolean subscribe, String reason, boolean success, String... channels) {
        super();
        setRequestID(requestID);
        this.subscribe = subscribe;
        this.success = success;
        this.reason = reason;
        this.channels = channels;
    }

    public boolean isSubscribe() {
        return subscribe;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getReason() {
        return reason;
    }
    
    public EventSubscriptionResponseMessage() {}

    public String[] getChannels() {
        return channels;
    }

    public void setChannels(String... channel) {
        this.channels = channel;
    }

    public void read(SofReader reader) throws SofException {
        this.subscribe = reader.readBoolean(1);
        this.channels = reader.readStringArray(2);
        this.setRequestID(reader.readInt(3));
        this.success = reader.readBoolean(4);
        this.reason = reader.readString(5);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, subscribe);
        writer.write(2, channels);
        writer.write(3, getRequestID());
        writer.write(4, success);
        writer.write(5, reason);
    }

    @Override public String toString() {
        return "NewSubscriptionResponseMessage [success=" +
               success +
               ", reason=" +
               reason +
               ", subscribe=" +
               subscribe +
               ", channels=" +
               Arrays.toString(channels) +
               "]";
    }

    
}

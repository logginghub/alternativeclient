package com.logginghub.connector.common.messages;

import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofReader;
import com.logginghub.sof.SofWriter;

public class EventSubscriptionRequestMessage extends BaseRequestResponseMessage implements SerialisableObject {

    private String[] channels;
    private boolean subscribe = true;

    public EventSubscriptionRequestMessage(int requestID, boolean subscribe, String... channels) {
        super();
        this.channels = channels;
        this.subscribe = subscribe;
        setRequestID(requestID);
    }

    public EventSubscriptionRequestMessage() {}

    public void setSubscribe(boolean subscribe) {
        this.subscribe = subscribe;
    }

    public boolean isSubscribe() {
        return subscribe;
    }

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
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, subscribe);
        writer.write(2, channels);
        writer.write(3, getRequestID());
    }

}

package com.logginghub.connector.common.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.logginghub.connector.common.LoggingMessage;
import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofReader;
import com.logginghub.sof.SofWriter;

public class ChannelMessage implements SerialisableObject, LoggingMessage {

    private String[] channel;
    private SerialisableObject payload;

    public ChannelMessage() {}

    public ChannelMessage(String channel, SerialisableObject payload) {
        super();
        this.channel = parseChannel(channel);
        this.payload = payload;
    }

    public static String[] parseChannelSplit(String channel) {
        String trimmed = channel.trim();
        return trimmed.split("\\\\");
    }

    public static String[] parseChannel(String channel) {

        String trimmed = channel.trim();

        List<String> channels = new ArrayList<String>();

        int start = 0;
        int end = 0;

        int size = trimmed.length();
        for (int i = 0; i < size; i++) {
            char c = trimmed.charAt(i);
            if (c == '/' || c == '\\') {
                channels.add(trimmed.subSequence(start, end).toString().trim());
                start = end + 1;
                end++;
            }
            else {
                end++;
            }
        }

        if (start < size) {
            channels.add(trimmed.subSequence(start, size).toString().trim());
        }

        return channels.toArray(new String[channels.size()]);

    }

    public void read(SofReader reader) throws SofException {
        this.channel = reader.readStringArray(1);
        this.payload = (SerialisableObject) reader.readObject(2);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, channel);
        writer.write(2, payload);
    }

    public String[] getChannel() {
        return channel;
    }

    public SerialisableObject getPayload() {
        return payload;
    }

    @Override public String toString() {
        return "ChannelMessage [channel=" + Arrays.toString(channel) + ", payload=" + payload + "]";
    }

    
}

package com.logginghub.connector.common;

import java.nio.ByteBuffer;

import com.logginghub.utils.ExpandingByteBuffer;

public class LogEventCollectionCodex {

    public static LogEventCollection decode(ByteBuffer buffer) throws PartialMessageException {
        int size = buffer.getInt();

        LogEventCollection collection = new LogEventCollection();

        for (int i = 0; i < size; i++) {
            LogEvent decoded = LogEventCodex.decode(buffer);
            collection.add(decoded);
        }

        return collection;
    }

    public static void encode(ExpandingByteBuffer expandingBuffer, LogEventCollection collection) {
        int size = collection.size();

        expandingBuffer.putInt(size);

        for (LogEvent logEvent : collection) {
            LogEventCodex.encode(expandingBuffer, logEvent);
        }
    }
}

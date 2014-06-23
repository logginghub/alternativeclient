package com.logginghub.connector.common;

import com.logginghub.utils.Logger;
import com.logginghub.utils.StringUtils;

public class LogEventBuilder {

    private DefaultLogEvent event = LogEventFactory.createFullLogEvent1();

    public LogEventBuilder() {
        event.setLocalCreationTimeMillis(System.currentTimeMillis());
    }

    public static LogEventBuilder start() {
        return new LogEventBuilder();
    }

    public LogEventBuilder setLevel(int level) {
        event.setLevel(level);
        return this;
    }

    public LogEventBuilder setSequenceNumber(long sequenceNumber) {
        event.setSequenceNumber(sequenceNumber);
        return this;
    }

    public LogEventBuilder setSourceClassName(String sourceClassName) {
        event.setSourceClassName(sourceClassName);
        return this;
    }

    public LogEventBuilder setSourceMethodName(String sourceMethodName) {
        event.setSourceMethodName(sourceMethodName);
        return this;
    }

    public LogEventBuilder setChannel(String channel) {
        event.setChannel(channel);
        return this;
    }

    public LogEventBuilder setMessage(String message) {
        event.setMessage(message);
        return this;
    }

    public LogEventBuilder setThreadName(String threadName) {
        event.setThreadName(threadName);
        return this;
    }

    public LogEventBuilder setSourceApplication(String sourceApplication) {
        event.setSourceApplication(sourceApplication);
        return this;
    }

    public LogEventBuilder setSourceHost(String sourceHost) {
        event.setSourceHost(sourceHost);
        return this;
    }

    public LogEventBuilder setSourceAddress(String sourceAddress) {
        event.setSourceAddress(sourceAddress);
        return this;
    }

    public LogEventBuilder setLocalCreationTimeMillis(long millis) {
        event.setLocalCreationTimeMillis(millis);
        return this;
    }

    public DefaultLogEvent toLogEvent() {
        return event;
    }

    public LogEventBuilder setMessage(String format, Object... params) {
        setMessage(StringUtils.format(format, params));
        return this;
    }

    public LogEventBuilder setFormattedException(String format, Object... params) {
        if (format == null) {
            event.setFormattedException(null);
        }
        else {
            event.setFormattedException(StringUtils.format(format, params));
        }
        return this;
    }

    public LogEventBuilder setPid(int pid) {
        event.setPid(pid);
        return this;
    }

    public LogEventBuilder setLoggerName(String loggerName) {
        event.setLoggerName(loggerName);
        return this;
    }

    public LogEventBuilder setFormattedObject(String... string) {
        event.setFormattedObject(string);
        return this;
    }

    public LogEventBuilder setLevel(String level) {
        event.setLevel(Logger.parseLevel(level));
        return this;
    }

    public static DefaultLogEvent create(long time, int level, String message) {
        return LogEventBuilder.start().setLocalCreationTimeMillis(time).setLevel(level).setMessage(message).toLogEvent();
    }

}

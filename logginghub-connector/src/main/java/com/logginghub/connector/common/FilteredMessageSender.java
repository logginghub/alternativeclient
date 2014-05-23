package com.logginghub.connector.common;

import com.logginghub.utils.Destination;

public interface FilteredMessageSender extends LoggingMessageSender, Destination<LogEvent> {
    int getLevelFilter();
}

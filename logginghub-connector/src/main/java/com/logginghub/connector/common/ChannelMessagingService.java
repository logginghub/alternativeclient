package com.logginghub.connector.common;

import com.logginghub.connector.common.messages.ChannelMessage;
import com.logginghub.utils.Destination;

public interface ChannelMessagingService {
    void send(ChannelMessage message) throws LoggingMessageSenderException;
    void subscribe(String channel, Destination<ChannelMessage> destination);
    void unsubscribe(String channel, Destination<ChannelMessage> destination);
}

package com.logginghub.connector.common;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import com.logginghub.connector.common.messages.ChannelMessage;
import com.logginghub.connector.common.messages.ChannelSubscriptionRequestMessage;
import com.logginghub.connector.common.messages.ChannelSubscriptionResponseMessage;
import com.logginghub.connector.common.messages.EventSubscriptionRequestMessage;
import com.logginghub.connector.common.messages.EventSubscriptionResponseMessage;
import com.logginghub.connector.common.messages.FilterRequestMessage;
import com.logginghub.connector.common.messages.HistoricalIndexRequest;
import com.logginghub.connector.common.messages.RequestResponseMessage;
import com.logginghub.connector.common.messages.SubscriptionRequestMessage;
import com.logginghub.connector.common.messages.SubscriptionResponseMessage;
import com.logginghub.connector.common.messages.UnsubscriptionRequestMessage;
import com.logginghub.connector.common.messages.UnsubscriptionResponseMessage;
import com.logginghub.utils.Destination;
import com.logginghub.utils.ExceptionPolicy;
import com.logginghub.utils.ExceptionPolicy.Policy;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.Handler;
import com.logginghub.utils.LatchFuture;
import com.logginghub.utils.Logger;

/**
 * Wraps the business logic side of being a logging client, backed by a SocketConnector to do the
 * donkey work.
 * 
 * @author admin
 */
public class SocketClient extends AbstractLoggingMessageSource implements LoggingMessageSender, Closeable, LogEventListener, ChannelMessagingService {
    private Set<String> autoChannelSubscriptions = new HashSet<String>();
    private boolean autoGlobalSubscription = true;

    private boolean autoSubscribe = true;
    private SocketConnector connector;
    private ExceptionPolicy exceptionPolicy = new ExceptionPolicy(Policy.SystemErr);
    private int levelFilter = Level.ALL.intValue();
    private AtomicInteger nextRequestID = new AtomicInteger(0);

    private Map<Integer, Handler<LoggingMessage>> requestResponseHandlers = new HashMap<Integer, Handler<LoggingMessage>>();

    
    
    // private List<LoggingMessage> queued = new ArrayList<LoggingMessage>();

    private SubscriptionController<Destination<ChannelMessage>> subscriptionController = new SubscriptionController<Destination<ChannelMessage>>() {
        @Override protected Future<Boolean> handleFirstSubscription(String channel) {
            return handleFirstSubscriptionInternal(channel);
        }

        @Override protected Future<Boolean> handleLastSubscription(String channel) {
            return handleLastSubscriptionInternal(channel);
        }
    };

    private static final Logger logger = Logger.getLoggerFor(SocketClient.class);

    /**
     * Convenience methods to create a new socket client, and automatically connect and attach the
     * provided listener.
     * 
     * @param inetSocketAddress
     * @param autoSubscribe
     * @param eventBucket
     * @return
     * @throws ConnectorException
     */
    public static SocketClient connect(InetSocketAddress connectionPoint, boolean autoSubscribe, LogEventListener listener) throws ConnectorException {
        SocketClient client = new SocketClient();
        client.addConnectionPoint(connectionPoint);
        client.setAutoSubscribe(autoSubscribe);
        client.addLogEventListener(listener);
        client.connect();
        return client;
    }

    public SocketClient() {
        this("");
    }

    public SocketClient(SocketConnector connector) {
        this.connector = connector;

        connector.addLoggingMessageListener(new LoggingMessageListener() {
            public void onNewLoggingMessage(LoggingMessage message) {
                handleMessage(message);
            }
        });

        connector.addSocketConnectorListener(new SocketConnectorListener() {
            public void onConnectionEstablished() {
                // sendQueued();
                reestablishSubscriptions();
            }

            public void onConnectionLost(String reason) {}
        });
    }

    // protected void sendQueued() {
    // List<LoggingMessage> temp = new ArrayList<LoggingMessage>();
    // synchronized (queued) {
    // temp.addAll(queued);
    // queued.clear();
    // }
    //
    // for (LoggingMessage loggingMessage : temp) {
    // try {
    // if(connector.isDebug()) {
    // connector.debug("Sending queued message : {}", loggingMessage);
    // }
    // send(loggingMessage);
    // }
    // catch (LoggingMessageSenderException e) {
    // if (!isConnected()) {
    // // That's just bad bloody luck, requeue it
    // synchronized (queued) {
    // queued.add(loggingMessage);
    // }
    // }
    // else {
    // throw new FormattedRuntimeException(e, "Failed to send message that had been queued : '{}'",
    // loggingMessage);
    // }
    // }
    // }
    // }

    public SocketClient(String name) {
        this(new SocketConnector(name));
    }

    public void addAutoSubscription(String channel) {
        synchronized (autoChannelSubscriptions) {
            autoChannelSubscriptions.add(channel);
        }
    }

    public void addConnectionPoint(InetSocketAddress address) {
        connector.addConnectionPoint(address);
    }

    public void addConnectionPoints(List<InetSocketAddress> inetSocketAddressList) {
        for (InetSocketAddress inetSocketAddress : inetSocketAddressList) {
            addConnectionPoint(inetSocketAddress);
        }
    }

    public Future<Boolean> addSubscription(String channel, Destination<ChannelMessage> destination) {
        return subscriptionController.addSubscription(channel, destination);
    }

    public void close() {
        connector.close();
    }

    public void connect() throws ConnectorException {
        logger.fine("Socket client connecting...");
        connector.connect();
        if (autoSubscribe) {
            try {
                // Check global subscriptions
                if (autoGlobalSubscription) {
                    logger.fine("Auto-subscribe is true, so sending subscription message");
                    subscribe();
                }

                // Channel subscriptions
                for (String channel : autoChannelSubscriptions) {
                    logger.info("Auto-subscribe is true, so sending subscription message for channel '{}'", channel);
                    subscribe(channel);
                }

                // If someone has set a level filter, need to pass that on as well
                if (levelFilter != Level.ALL.intValue()) {
                    logger.fine("A level filter has been set, sending to hub");
                    sendLevelFilter();
                }
            }
            catch (LoggingMessageSenderException e) {
                throw new ConnectorException("Failed to auto-subscribe", e);
            }
        }
    }

    public void disconnect() {
        connector.disconnect();
    }

    public void flush() {
        connector.flush();
    }

    public boolean getAutoSubscribe() {
        return autoSubscribe;
    }

    public SocketConnector getConnector() {
        return connector;
    }

    public int getNextRequestID() {
        return nextRequestID.getAndIncrement();
    }

    public boolean isConnected() {
        return connector.isConnected();
    }

    public void onNewLogEvent(LogEvent event) {
        try {
            send(new LogEventMessage(event));
        }
        catch (LoggingMessageSenderException e) {
            throw new FormattedRuntimeException("Failed to send log event message", e);
        }
    }

    public void removeAutoSubscription(String channel) {
        synchronized (autoChannelSubscriptions) {
            autoChannelSubscriptions.remove(channel);
        }
    }

    public void removeConnectionPoint(InetSocketAddress inetSocketAddress) {
        connector.removeConnectionPoint(inetSocketAddress);
    }

    public void removeSubscription(String channel, Destination<ChannelMessage> destination) {
        subscriptionController.removeSubscription(channel, destination);
    }

    public void replaceConnectionList(List<InetSocketAddress> parseAddressAndPortList) {
        connector.replaceConnectionList(parseAddressAndPortList);
    }

    public void send(LoggingMessage message) throws LoggingMessageSenderException {
        connector.send(message);
    }

    public void sendBlocking(LoggingMessage message) throws LoggingMessageSenderException {
        connector.sendBlocking(message);
    }

    public void sendHistoricalIndexRequest(long from, long to) {
        
        logger.info("Sending history index request");
        
        try {
            send(new HistoricalIndexRequest(from, to));
        }
        catch (LoggingMessageSenderException e) {
            throw new FormattedRuntimeException("Failed to send log event message", e);
        }
    }

    public void sendWhenConnected(LoggingMessage message) {
        connector.sendWhenConnected(message);
    }

    public void setAutoGlobalSubscription(boolean autoGlobalSubscription) {
        this.autoGlobalSubscription = autoGlobalSubscription;
    }

    public void setAutoSubscribe(boolean autoSubscribe) {
        this.autoSubscribe = autoSubscribe;
    }

    public void setDebug(boolean b) {
        connector.setDebug(b);
    }

    public void setForceFlush(boolean forceFlush) {
        connector.setForceFlush(forceFlush);
    }

    public void setLevelFilter(int levelFilter) throws LoggingMessageSenderException {
        this.levelFilter = levelFilter;
        sendLevelFilter();
    }

    public void setWriteQueueMaximumSize(int writeQueueMaximumSize) {
        connector.setWriteQueueMaximumSize(writeQueueMaximumSize);
    }

    public void setWriteQueueOverflowPolicy(SocketConnection.SlowSendingPolicy policy) {
        connector.setWriteQueueOverflowPolicy(policy);
    }

    public void subscribe() throws LoggingMessageSenderException {
        final CountDownLatch latch = new CountDownLatch(1);
        addLoggingMessageListener(new LoggingMessageListener() {
            public void onNewLoggingMessage(LoggingMessage message) {
                if (message instanceof SubscriptionResponseMessage) {
                    latch.countDown();
                }
            }
        });

        SubscriptionRequestMessage subscriptionMessage = new SubscriptionRequestMessage();
        send(subscriptionMessage);
        try {
            if (latch.await(10, TimeUnit.SECONDS)) {
                // Happy
            }
            else {
                throw new LoggingMessageSenderException("Subscribe request failed; timed out waiting for the response message");
            }
        }
        catch (InterruptedException e) {
            throw new LoggingMessageSenderException("Subscribe request may have failed; the thread was interupted waiting for the response message");
        }
    }

    public void subscribe(String... channels) throws LoggingMessageSenderException {
        // TODO : multiple channel subscriptions
        for (String channel : channels) {
            subscribe(channel);
        }
    }

    public void subscribe(String channel) throws LoggingMessageSenderException {
        final CountDownLatch latch = new CountDownLatch(1);
        addLoggingMessageListener(new LoggingMessageListener() {
            public void onNewLoggingMessage(LoggingMessage message) {
                if (message instanceof EventSubscriptionResponseMessage) {
                    latch.countDown();
                }
            }
        });

        EventSubscriptionRequestMessage subscriptionMessage = new EventSubscriptionRequestMessage(getNextRequestID(), true, channel);
        send(subscriptionMessage);
        try {
            if (latch.await(10, TimeUnit.SECONDS)) {
                // Happy
            }
            else {
                throw new LoggingMessageSenderException("Channel subscribe request failed; timed out waiting for the response message");
            }
        }
        catch (InterruptedException e) {
            throw new LoggingMessageSenderException("Channel subscribe request may have failed; the thread was interupted waiting for the response message");
        }
    }

    public void subscribe(String channel, Destination<ChannelMessage> destination) {
        subscriptionController.addSubscription(channel, destination);
    }
    
    public void subscribe(Destination<ChannelMessage> destination, String... channels) {
        for (String channel : channels) {
            // TODO : handle bulk subscriptions all the way through the subscription stack
            subscriptionController.addSubscription(channel, destination);
        }
    }

    // public void subscribeToAggregatedPatternEvents(AggregationKey... keys) {
    // sendWhenConnected(new AggregatedPatternDataSubscriptionRequestMessage(true, keys));
    // }

    public void unsubscribe() throws LoggingMessageSenderException {
        final CountDownLatch latch = new CountDownLatch(1);
        addLoggingMessageListener(new LoggingMessageListener() {
            public void onNewLoggingMessage(LoggingMessage message) {
                if (message instanceof UnsubscriptionResponseMessage) {
                    latch.countDown();
                }
            }
        });

        UnsubscriptionRequestMessage unsubscriptionMessage = new UnsubscriptionRequestMessage();
        send(unsubscriptionMessage);
        try {
            if (latch.await(10, TimeUnit.SECONDS)) {
                // Happy
            }
            else {
                throw new LoggingMessageSenderException("Subscribe request failed; timed out waiting for the response message");
            }
        }
        catch (InterruptedException e) {
            throw new LoggingMessageSenderException("Subscribe request may have failed; the thread was interupted waiting for the response message");
        }
    }

    public void unsubscribe(String channel, Destination<ChannelMessage> destination) {
        subscriptionController.removeSubscription(channel, destination);
    }

    // public void unsubscribeToAggregatedPatternEvents(final AggregationKey... keys) {
    // // TODO : test me, not sure this works, and its different from the subscribe path (ie
    // // blocking)
    // final CountDownLatch latch = new CountDownLatch(1);
    // addLoggingMessageListener(new LoggingMessageListener() {
    // public void onNewLoggingMessage(LoggingMessage message) {
    // if (message instanceof AggregatedPatternDataSubscriptionResponseMessage) {
    // latch.countDown();
    // }
    // }
    // });
    //
    // try {
    // connector.send(new AggregatedPatternDataSubscriptionRequestMessage(false, keys));
    //
    // try {
    // if (latch.await(10, TimeUnit.SECONDS)) {
    // // Happy
    // }
    // else {
    // throw new
    // LoggingMessageSenderException("AggregatedPatternDataSubscription request failed; timed out waiting for the response message");
    // }
    // }
    // catch (InterruptedException e) {
    // throw new
    // LoggingMessageSenderException("AggregatedPatternDataSubscription request may have failed; the thread was interupted waiting for the response message");
    // }
    // }
    // catch (LoggingMessageSenderException e) {
    // throw new
    // FormattedRuntimeException("Failed to send AggregatedPatternDataSubscription message", e);
    // }
    //
    // }

    private void sendLevelFilter() throws LoggingMessageSenderException {
        FilterRequestMessage filterMessage = new FilterRequestMessage(levelFilter);
        send(filterMessage);
    }

    protected void handleMessage(LoggingMessage message) {
        logger.fine("New message received by the socket client '{}'", message);

        fireNewMessage(message);

        if (message instanceof ChannelMessage) {
            ChannelMessage channelMessage = (ChannelMessage) message;
            subscriptionController.dispatch(channelMessage, null);
        }

        if (message instanceof RequestResponseMessage) {
            RequestResponseMessage requestResponseMessage = (RequestResponseMessage) message;
            int requestID = requestResponseMessage.getRequestID();
            Handler<LoggingMessage> handler = requestResponseHandlers.get(requestID);
            if (handler != null) {
                if (handler.handle(message)) {
                    requestResponseHandlers.remove(requestID);
                }
            }
        }
    }

    protected void reestablishSubscriptions() {
        Set<String> channels = subscriptionController.getChannels();
        if (channels.size() > 0) {
            String[] channelsArray = channels.toArray(new String[channels.size()]);
            ChannelSubscriptionRequestMessage subscriptionMessage = new ChannelSubscriptionRequestMessage(getNextRequestID(), true, channelsArray);
            try {
                send(subscriptionMessage);
            }
            catch (LoggingMessageSenderException e) {
                exceptionPolicy.handle(e);
            }
        }

    }

    private Future<Boolean> handleFirstSubscriptionInternal(String channel) {
        final LatchFuture<Boolean> future = new LatchFuture<Boolean>();

        final ChannelSubscriptionRequestMessage request = new ChannelSubscriptionRequestMessage(getNextRequestID(), true, channel);

        requestResponseHandlers.put(request.getRequestID(), new Handler<LoggingMessage>() {
            public boolean handle(LoggingMessage t) {
                boolean handled = false;
                if (t instanceof ChannelSubscriptionResponseMessage) {
                    ChannelSubscriptionResponseMessage response = (ChannelSubscriptionResponseMessage) t;
                    if (response.getRequestID() == request.getRequestID()) {
                        future.trigger(response.isSuccess());
                        handled = true;
                    }

                }
                return handled;
            }
        });

        boolean sent = false;
        if (isConnected()) {
            try {
                send(request);
                sent = true;
            }
            catch (LoggingMessageSenderException e) {
                sent = false;
                // TODO : this is probably ok, as the subscription will get re-created automatically
                // when we reconnect
            }
        }

        // if (!sent) {
        // // Queue it up to be sent when we make a connection
        // synchronized (queued) {
        // queued.add(request);
        // }
        // }

        return future;
    }

    private Future<Boolean> handleLastSubscriptionInternal(String channel) {
        try {
            send(new ChannelSubscriptionRequestMessage(getNextRequestID(), false, channel));
        }
        catch (final LoggingMessageSenderException e) {
            throw new FormattedRuntimeException("Failed to send message", e);
        }

        // TODO : implement unsubscribe futures
        return null;
    }

    public void send(ChannelMessage message) throws LoggingMessageSenderException {
        connector.send(message);
    }

    public String getName() {
        return connector.getName();

    }

}

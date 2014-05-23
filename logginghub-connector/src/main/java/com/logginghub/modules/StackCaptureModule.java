package com.logginghub.modules;

import java.net.InetSocketAddress;

import com.logginghub.connector.common.ChannelMessagingService;
import com.logginghub.connector.common.Channels;
import com.logginghub.connector.common.ConnectorException;
import com.logginghub.connector.common.LoggingMessageSender;
import com.logginghub.connector.common.SocketClient;
import com.logginghub.connector.common.SocketClientManager;
import com.logginghub.connector.common.messages.ChannelMessage;
import com.logginghub.utils.Destination;
import com.logginghub.utils.StackStrobeRequest;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.VLPorts;

public class StackCaptureModule implements Module<StackCaptureConfiguration> {

    private LoggingMessageSender loggingMessageSender;

    private StackCaptureController controller;

    private ChannelMessagingService channelSubscriptions;

    // TODO : allow this to be configurable via the Container
    public StackCaptureModule(LoggingMessageSender loggingMessageSender, ChannelMessagingService channelSubscriptions) {
        this.loggingMessageSender = loggingMessageSender;
        this.channelSubscriptions = channelSubscriptions;
        this.controller = new StackCaptureController();
    }

    public void setLoggingMessageSender(LoggingMessageSender loggingMessageSender) {
        this.loggingMessageSender = loggingMessageSender;
    }

    public void setChannelSubscriptions(ChannelMessagingService channelSubscriptions) {
        this.channelSubscriptions = channelSubscriptions;
    }

    public void configure(StackCaptureConfiguration configuration, ServiceDiscovery discovery) {
        this.controller.configure(loggingMessageSender,
                                  TimeUtils.parseInterval(configuration.getSnapshotInterval()),
                                  configuration.getEnvironment(),
                                  configuration.getHost(),
                                  configuration.getInstanceType(),
                                  configuration.getInstanceNumber());
    }

    public void start() {
        stop();

        channelSubscriptions.subscribe(Channels.strobeRequests, new Destination<ChannelMessage>() {
            public void send(ChannelMessage t) {
                StackStrobeRequest request = (StackStrobeRequest) t.getPayload();
                controller.executeStrobe(request);
            }
        });

        controller.start();
    }

    public void stop() {
        if (controller != null) {
            controller.stop();
        }
    }

    public static void main(String[] args) throws ConnectorException {

        SocketClient client = new SocketClient("StackCaptureClient");
        client.addConnectionPoint(new InetSocketAddress(VLPorts.getSocketHubDefaultPort()));
        client.setAutoGlobalSubscription(false);
        client.setAutoSubscribe(false);
        client.connect();

        SocketClientManager manager = new SocketClientManager(client);
        manager.start();

        StackCaptureModule capture = new StackCaptureModule(client, client);
        StackCaptureConfiguration config = new StackCaptureConfiguration();
        config.setSnapshotInterval("0");
        config.setEnvironment("local");
        config.setInstanceType("StackCaptureModule");
        config.setInstanceNumber(1);
        capture.configure(config, null);
        capture.start();

        try {
            Thread.sleep(Long.MAX_VALUE);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}

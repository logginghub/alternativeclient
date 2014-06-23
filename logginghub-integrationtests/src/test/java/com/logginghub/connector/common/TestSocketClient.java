package com.logginghub.connector.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.logginghub.connector.common.messages.ChannelMessage;
import com.logginghub.connector.common.messages.LogEventMessage;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.ThreadUtils;
import com.vertexlabs.logging.modules.ChannelSubscriptionsModule;
import com.vertexlabs.logging.modules.configuration.ChannelSubscriptionsConfiguration;
import com.vertexlabs.logging.servers.SocketHub;
import com.vertexlabs.logging.servers.SocketHubInterface;
import com.vertexlabs.utils.module.ConfigurableServiceDiscovery;

public class TestSocketClient {

    @Rule public ExpectedException exception = ExpectedException.none();

    private SocketHub hub;
    private SocketClient clientA;
    private SocketClient clientB;

    private DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
    private DefaultLogEvent event2 = LogEventFactory.createFullLogEvent1();
    private DefaultLogEvent event3 = LogEventFactory.createFullLogEvent1();

    private LogEventMessage message1 = new LogEventMessage(event1);
    private LogEventMessage message2 = new LogEventMessage(event2);
    private LogEventMessage message3 = new LogEventMessage(event3);

    @After public void cleanup() {
        FileUtils.closeQuietly(hub, clientA, clientB);
    }

    @Test public void test_connect_with_hub() throws IOException, ConnectorException {

        hub = SocketHub.createTestHub();
        hub.start();

        clientA = new SocketClient();
        clientA.addConnectionPoint(hub.getConnectionPoint());
        clientA.connect();
        assertThat(clientA.isConnected(), is(true));

    }

    @Test public void test_connect_without_hub() throws IOException, ConnectorException {

        clientA = new SocketClient();
        clientA.addConnectionPoint(new InetSocketAddress("localhost", NetUtils.findFreePort()));

        try {
            clientA.connect();
            fail("Connect should have failed");
        }
        catch (ConnectorException ce) {
            assertThat(ce.getMessage(), containsString("failed to establish a connection with any of the 1 connection points"));
        }
        assertThat(clientA.isConnected(), is(false));
    }

    @Test public void test_connect_when_hub_comes_back() throws IOException, ConnectorException {

        hub = SocketHub.createTestHub();

        clientA = new SocketClient();
        clientA.addConnectionPoint(hub.getConnectionPoint());

        try {
            clientA.connect();
            fail("Connect should have failed");
        }
        catch (ConnectorException ce) {
            assertThat(ce.getMessage(), containsString("failed to establish a connection with any of the 1 connection points"));
        }
        assertThat(clientA.isConnected(), is(false));

        hub.start();
        clientA.connect();
        assertThat(clientA.isConnected(), is(true));
    }

    @Test public void test_subscription_when_disconnected() throws IOException, ConnectorException {

        hub = SocketHub.createTestHub();

        // TODO : we need to move this out into a separate class that builds a "standard" hub
        // configuration with all the modules
        final ChannelSubscriptionsModule channelSubscriptionsModule = new ChannelSubscriptionsModule();
        ConfigurableServiceDiscovery disco = new ConfigurableServiceDiscovery();
        disco.bind(SocketHubInterface.class, hub);
        channelSubscriptionsModule.configure(new ChannelSubscriptionsConfiguration(), disco);
        channelSubscriptionsModule.start();
        // end hack

        clientA = new SocketClient();
        clientA.setDebug(true);
        clientA.addConnectionPoint(hub.getConnectionPoint());

        try {
            clientA.connect();
            fail("Connect should have failed");
        }
        catch (ConnectorException ce) {
            assertThat(ce.getMessage(), containsString("failed to establish a connection with any of the 1 connection points"));
        }
        assertThat(clientA.isConnected(), is(false));

        clientA.subscribe("channel", new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {

            }
        });

        hub.start();
        clientA.connect();
        assertThat(clientA.isConnected(), is(true));

        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return channelSubscriptionsModule.getSubscriptions().getDestinations("channel").size() == 1;
            }
        });

    }

    @Test public void test_connect_when_hub_goes_down() throws IOException, ConnectorException {

        // Fire up the hub
        hub = SocketHub.createTestHub();
        hub.start();

        // Connect the client
        clientA = new SocketClient();
        clientA.addConnectionPoint(hub.getConnectionPoint());
        clientA.connect();
        assertThat(clientA.isConnected(), is(true));

        // Kill the hub and make sure the client gets disconnected (possible
        // race condition?)
        hub.stop();

        ThreadUtils.untilTrue(5, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return !clientA.isConnected();
            }
        });
        assertThat(clientA.isConnected(), is(false));

        // Attempt to connect whilst its down
        try {
            clientA.connect();
            fail("Connect should have failed");
        }
        catch (ConnectorException ce) {
            assertThat(ce.getMessage(), containsString("failed to establish a connection with any of the 1 connection points"));
        }
        assertThat(clientA.isConnected(), is(false));

        // Restart and successfully connect
        hub.start();
        clientA.connect();
        assertThat(clientA.isConnected(), is(true));
    }

    @Test public void test_pid() throws IOException, ConnectorException, LoggingMessageSenderException {

        hub = SocketHub.createTestHub();
        hub.start();

        clientA = new SocketClient();
        clientA.addConnectionPoint(hub.getConnectionPoint());
        clientA.connect();
        assertThat(clientA.isConnected(), is(true));
        
        clientB = new SocketClient();
        clientB.addConnectionPoint(hub.getConnectionPoint());
        clientB.connect();
        assertThat(clientB.isConnected(), is(true));
        
        LogEventBucket bucket = new LogEventBucket();        
        clientB.addLogEventListener(bucket);
        
        int pid = AppenderHelper.getPID();
        clientA.send(new LogEventMessage(LogEventBuilder.start().setMessage("Test message").setPid(pid).toLogEvent()));
        
        bucket.waitForMessages(1);
        
        assertThat(bucket.get(0).getPid(), is(greaterThan(0)));

    }
    
//    @Test public void test_connect_with_autosubcribe_on() throws IOException, ConnectorException {
//
//        hub = SocketHub.createTestHub();
//        hub.start();
//
//        client = new SocketClient();
//        client.setAutoSubscribe(true);
//        client.addConnectionPoint(hub.getConnectionPoint());
//        client.connect();
//        LogEventBucket bucket = new LogEventBucket();
//        client.addLogEventListener(bucket);
//        assertThat(client.isConnected(), is(true));
//
//        List<com.vertexlabs.logging.interfaces.FilteredMessageSender> subscribedConnections = hub.getSubscribedConnections();
//        assertThat(subscribedConnections.size(), is(1));
// 
//        SocketConnection source = Mockito.mock(SocketConnection.class);
//        hub.onNewMessage(message1, source);
//
//        bucket.waitForMessages(1);
//        assertThat(bucket.size(), is(1));
//        assertThat(bucket.get(0).getMessage(), is(event1.getMessage()));
//
//    }
//
//    @Test public void test_connect_with_autosubcribe_off() throws IOException, ConnectorException {
//
//        hub = SocketHub.createTestHub();
//        hub.start();
//
//        client = new SocketClient();
//        client.setAutoSubscribe(false);
//        client.addConnectionPoint(hub.getConnectionPoint());
//        client.connect();
//        LogEventBucket bucket = new LogEventBucket();
//        client.addLogEventListener(bucket);
//        assertThat(client.isConnected(), is(true));
//
//        List<com.vertexlabs.logging.interfaces.FilteredMessageSender> subscribedConnections = hub.getSubscribedConnections();
//        assertThat(subscribedConnections.size(), is(0));
//
//        SocketConnection source = Mockito.mock(SocketConnection.class);
//        hub.onNewMessage(message1, source);
//
//        ThreadUtils.sleep(1000);
//        assertThat(bucket.size(), is(0));
//    }
//
//    @Test public void test_connect_and_subscribe() throws IOException, ConnectorException, LoggingMessageSenderException {
//
//        hub = SocketHub.createTestHub();
//        hub.start();
//
//        client = new SocketClient();
//        client.setAutoSubscribe(false);
//        client.addConnectionPoint(hub.getConnectionPoint());
//        client.connect();
//        LogEventBucket bucket = new LogEventBucket();
//        client.addLogEventListener(bucket);
//        assertThat(client.isConnected(), is(true));
//
//        // We are not subscribed at this point
//        List<com.vertexlabs.logging.interfaces.FilteredMessageSender> subscribedConnections = hub.getSubscribedConnections();
//        assertThat(subscribedConnections.size(), is(0));
//
//        SocketConnection source = Mockito.mock(SocketConnection.class);
//        hub.onNewMessage(message1, source);
//
//        ThreadUtils.sleep(1000);
//        assertThat(bucket.size(), is(0));
//
//        // Now subscribe
//        client.subscribe();
//
//        assertThat(subscribedConnections.size(), is(1));
//        hub.onNewMessage(message1, source);
//
//        bucket.waitForMessages(1);
//        assertThat(bucket.size(), is(1));
//        assertThat(bucket.get(0).getMessage(), is(event1.getMessage()));
//        bucket.clear();
//
//        // Unsubscribe again
//        client.unsubscribe();
//
//        assertThat(subscribedConnections.size(), is(0));
//        hub.onNewMessage(message1, source);
//
//        ThreadUtils.sleep(1000);
//        assertThat(bucket.size(), is(0));
//    }
//
//    @Ignore
//            * cant get the queue to disconnect reliably, might be very ill concieved as the hub
//            * isn't actually stuck
//            
//    @Test public void test_write_queue_overflow_with_disconnect_policy() throws IOException, ConnectorException, LoggingMessageSenderException {
//
//        hub = SocketHub.createTestHub();
//        hub.start();
//
//        client = new SocketClient();
//        client.addConnectionPoint(hub.getConnectionPoint());
//        client.setWriteQueueMaximumSize(2);
//        client.setWriteQueueOverflowPolicy(SlowSendingPolicy.disconnect);
//        client.connect();
//        assertThat(client.isConnected(), is(true));
//
//        // Block up the hub
//        final Lock lock = new ReentrantLock();
//
//        final Bucket<LoggingMessage> hubBucket = new Bucket<LoggingMessage>();
//        hub.addAndSubscribeLocalListener(new FilteredMessageSender() {
//            public void send(LoggingMessage message) throws LoggingMessageSenderException {
//                hubBucket.add(message);
//                try {
//                    lock.lockInterruptibly();
//                    lock.unlock();
//                }
//                catch (InterruptedException e) {}
//            }
//
//            @Override public int getLevelFilter() {
//                return Level.ALL.intValue();
//            }
//
//            @Override public void send(LogEvent t) {
//                try {
//                    send(new LogEventMessage(t));
//                }
//                catch (LoggingMessageSenderException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        // Freeze the hub
//        lock.lock();
//
//        // Send some messages
//        client.send(message1);
//        client.send(message2);
//        assertThat(client.isConnected(), is(true));
//        client.flush();
//
//        // This one should tip us over the edge
//        client.send(message3);
//        assertThat(client.isConnected(), is(false));
//    }
//
//    @Test public void test_write_queue_overflow_with_discard_policy() throws IOException, ConnectorException, LoggingMessageSenderException {
//
//        hub = SocketHub.createTestHub();
//        hub.start();
//
//        client = new SocketClient();
//        client.addConnectionPoint(hub.getConnectionPoint());
//        client.setWriteQueueMaximumSize(2);
//        client.setWriteQueueOverflowPolicy(SlowSendingPolicy.discard);
//        client.connect();
//
//        assertThat(client.isConnected(), is(true));
//
//        // Block up the hub
//        final Lock lock = new ReentrantLock();
//
//        final Bucket<LoggingMessage> hubBucket = new Bucket<LoggingMessage>();
//        hub.addAndSubscribeLocalListener(new FilteredMessageSender() {
//            public void send(LoggingMessage message) throws LoggingMessageSenderException {
//                hubBucket.add(message);
//                try {
//                    lock.lockInterruptibly();
//                    lock.unlock();
//                }
//                catch (InterruptedException e) {}
//            }
//
//            @Override public int getLevelFilter() {
//                return Level.ALL.intValue();
//            }
//
//            @Override public void send(LogEvent t) {
//                try {
//                    send(new LogEventMessage(t));
//                }
//                catch (LoggingMessageSenderException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        // Freeze the hub
//        lock.lock();
//
//        // Send some messages
//        client.send(message1);
//        client.send(message2);
//        assertThat(client.isConnected(), is(true));
//        client.flush();
//
//        // This one should tip us over the edge
//        client.send(message3);
//        assertThat(client.isConnected(), is(true));
//
//        // Unlock the hub
//        lock.unlock();
//
//        hubBucket.waitForMessages(2);
//        assertThat(hubBucket.size(), is(2));
//        assertThat(hubBucket.get(0), is(instanceOf(LogEventMessage.class)));
//        assertThat(hubBucket.get(1), is(instanceOf(LogEventMessage.class)));
//
//        // We should have discarded event2 as it was the earliest one on the
//        // queue when we blocked
//        assertThat(((LogEventMessage) hubBucket.get(0)).getLogEvent().getMessage(), is(event1.getMessage()));
//        assertThat(((LogEventMessage) hubBucket.get(1)).getLogEvent().getMessage(), is(event3.getMessage()));
//    }
//
//    @Test public void test_write_queue_overflow_with_blocking_policy() throws IOException, ConnectorException, LoggingMessageSenderException {
//
//        hub = SocketHub.createTestHub();
//        hub.start();
//
//        client = new SocketClient();
//        client.addConnectionPoint(hub.getConnectionPoint());
//        client.setWriteQueueMaximumSize(2);
//        client.setWriteQueueOverflowPolicy(SlowSendingPolicy.block);
//        client.connect();
//
//        assertThat(client.isConnected(), is(true));
//
//        // Block up the hub
//        final Lock lock = new ReentrantLock();
//
//        final Bucket<LoggingMessage> hubBucket = new Bucket<LoggingMessage>();
//        hub.addAndSubscribeLocalListener(new FilteredMessageSender() {
//            public void send(LoggingMessage message) throws LoggingMessageSenderException {
//                hubBucket.add(message);
//                try {
//                    lock.lockInterruptibly();
//                    lock.unlock();
//                }
//                catch (InterruptedException e) {}
//            }
//
//            @Override public int getLevelFilter() {
//                return Level.ALL.intValue();
//            }
//
//            @Override public void send(LogEvent t) {
//                try {
//                    send(new LogEventMessage(t));
//                }
//                catch (LoggingMessageSenderException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        // Freeze the hub
//        lock.lock();
//
//        // Send some messages
//        client.send(message1);
//        client.send(message2);
//        assertThat(client.isConnected(), is(true));
//        client.flush();
//
//        // As the send method is going to block, we need to fire it off in
//        // another thread
//        Future<Boolean> future = ThreadUtils.execute(new Callable<Boolean>() {
//            public Boolean call() throws Exception {
//                client.send(message3);
//                return true;
//            }
//        });
//
//        // Make sure it blocked
//        assertThat(future.isDone(), is(false));
//
//        // Unlock the hub
//        lock.unlock();
//
//        hubBucket.waitForMessages(3);
//        assertThat(hubBucket.size(), is(3));
//        assertThat(hubBucket.get(0), is(instanceOf(LogEventMessage.class)));
//        assertThat(hubBucket.get(1), is(instanceOf(LogEventMessage.class)));
//        assertThat(hubBucket.get(2), is(instanceOf(LogEventMessage.class)));
//
//        // Double check the send unblocked correctly
//        assertThat(future.isDone(), is(true));
//
//        // We should have discarded event2 as it was the earliest one on the
//        // queue when we blocked
//        assertThat(((LogEventMessage) hubBucket.get(0)).getLogEvent().getMessage(), is(event1.getMessage()));
//        assertThat(((LogEventMessage) hubBucket.get(1)).getLogEvent().getMessage(), is(event2.getMessage()));
//        assertThat(((LogEventMessage) hubBucket.get(2)).getLogEvent().getMessage(), is(event3.getMessage()));
//    }
//
//    @Test public void test_send_blocking() throws IOException, ConnectorException, LoggingMessageSenderException {
//
//        hub = SocketHub.createTestHub();
//        hub.start();
//
//        client = new SocketClient();
//        client.addConnectionPoint(hub.getConnectionPoint());
//        client.setWriteQueueMaximumSize(2);
//        client.setWriteQueueOverflowPolicy(SlowSendingPolicy.block);
//        client.connect();
//
//        assertThat(client.isConnected(), is(true));
//
//        // Block up the hub
//        final Lock lock = new ReentrantLock();
//
//        final Bucket<LoggingMessage> hubBucket = new Bucket<LoggingMessage>();
//        hub.addAndSubscribeLocalListener(new FilteredMessageSender() {
//            public void send(LoggingMessage message) throws LoggingMessageSenderException {
//                hubBucket.add(message);
//                try {
//                    lock.lockInterruptibly();
//                    lock.unlock();
//                }
//                catch (InterruptedException e) {}
//            }
//
//            @Override public int getLevelFilter() {
//                return Level.ALL.intValue();
//            }
//
//            @Override public void send(LogEvent t) {
//                try {
//                    send(new LogEventMessage(t));
//                }
//                catch (LoggingMessageSenderException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        // Freeze the hub
//        lock.lock();
//
//        final Pointer<Integer> value = new Pointer<Integer>(0);
//        value.value = 0;
//        Future<Boolean> future = ThreadUtils.execute(new Callable<Boolean>() {
//            public Boolean call() throws Exception {
//                for (int i = 0; i < 1000; i++) {
//                    value.value = i;
//                    // Out.out("Sending {}", i);
//                    client.sendBlocking(message1);
//                    client.flush();
//                }
//                return true;
//            }
//        });
//
//        // Horrible code - need to think of a better way to wait until
//        // everything seized up!
//        boolean isStuck = false;
//        long stuckTime = 0;
//        int lastValue = -1;
//        while (!isStuck) {
//
//            if (value.value == lastValue) {}
//            else {
//                stuckTime = System.currentTimeMillis();
//                lastValue = value.value;
//            }
//
//            long elapsed = System.currentTimeMillis() - stuckTime;
//            if (elapsed > 1000) {
//                Out.out("Value is stuck at {}", lastValue);
//                isStuck = true;
//            }
//            else {
//                isStuck = false;
//            }
//        }
//
//        // Make sure it blocked
//        assertThat(future.isDone(), is(false));
//
//        // Unlock the hub
//        lock.unlock();
//
//        hubBucket.waitForMessages(1000);
//        assertThat(hubBucket.size(), is(1000));
//        assertThat(hubBucket.get(0), is(instanceOf(LogEventMessage.class)));
//
//        // Double check the send unblocked correctly
//        assertThat(future.isDone(), is(true));
//
//        // We should have discarded event2 as it was the earliest one on the
//        // queue when we blocked
//        assertThat(((LogEventMessage) hubBucket.get(0)).getLogEvent().getMessage(), is(event1.getMessage()));
//    }
}

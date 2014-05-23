package com.logginghub.connector.common;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import com.logginghub.utils.Logger;
import com.logginghub.utils.WorkerThread;

/**
 * Convenience thread that manages a SocketClient and makes sure it stays connected
 * 
 * @author James
 * 
 */
public class SocketClientManager extends WorkerThread {
    private final SocketClient client;
    private long reconnectionTime = 3000;

    private static final Logger logger = Logger.getLoggerFor(SocketClientManager.class);
    private boolean autoSubscribe;

    public enum State {
        NotConnected,
        Connecting,
        Connected
    };

    private State currentState = State.NotConnected;

    private List<SocketClientManagerListener> listeners = new CopyOnWriteArrayList<SocketClientManagerListener>();

    public SocketClientManager(SocketClient client) {
        super("SocketClientManager");
        this.client = client;
    }

    public void addSocketClientManagerListener(SocketClientManagerListener listener) {
        listeners.add(listener);
    }

    public void removeSocketClientManagerListener(SocketClientManagerListener listener) {
        listeners.remove(listener);
    }

    private void fireStateChange(State from, State to) {
        for (SocketClientManagerListener socketClientManagerListener : listeners) {
            socketClientManagerListener.onStateChanged(from, to);
        }
    }

    @Override protected void onRun() throws Throwable {
        // TODO : this code is riddled with race conditions, and the latch.await() will keep us
        // waiting for ever I think
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            if (!client.isConnected()) {
                setState(State.Connecting);
                logger.fine("Attempting connection");
                client.connect();
            }
            setState(State.Connected);
            logger.fine("Connected");
            SocketConnector connector = client.getConnector();

            // TODO : I'm worried we might be leaking these listeners as we never remove them!
            ConnectionListener reconnectionListener = new ConnectionListener() {
                public void onConnectionClosed(String reason) {
                    // Only do this is the manager is still actually running
                    if (isRunning()) {
                        setState(State.NotConnected);
                        reconnectSleep();
                        latch.countDown();
                    }
                }
            };
            
            // TODO : if we've been asynchronously disconnected, the current connection will be null and this will blow up
            connector.getCurrentConnection().addConnectionListener(reconnectionListener);

            latch.await();
        }
        catch (ConnectorException connectorException) {
            logger.fine(connectorException, "Failed to make a hub connection : {}", connectorException.getMessage());
            // getExceptionHandler().handleException("Socket connection failed",
            // connectorException);
            setState(State.NotConnected);
            // Ignore this and just keep trying to reconnect
            reconnectSleep();
        }
    }

    private void setState(State connecting) {
        State oldState = currentState;
        currentState = connecting;
        fireStateChange(oldState, currentState);
    }

    private void reconnectSleep() {
        try {
            Thread.sleep(reconnectionTime);
        }
        catch (InterruptedException e) {}
    }

    public SocketClient getClient() {
        return client;
    }

}

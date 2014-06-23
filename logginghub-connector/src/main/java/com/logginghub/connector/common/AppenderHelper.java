package com.logginghub.connector.common;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.NoSuchElementException;

import com.logginghub.connector.common.SocketConnection.SlowSendingPolicy;
import com.logginghub.connector.common.messages.LogEventMessage;
import com.logginghub.utils.CpuLogger;
import com.logginghub.utils.GCWatcher;
import com.logginghub.utils.HeapLogger;
import com.logginghub.utils.Logger;
import com.logginghub.utils.NotImplementedException;
import com.logginghub.utils.RunnableWorkerThread;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.VLPorts;

import edu.emory.mathcs.backport.java.util.concurrent.BlockingDeque;
import edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingDeque;

/**
 * Because the j.u.l and log4j handlers/appenders need to extend base classes, we have to include
 * our generic implementation via composition.
 * 
 * @author James
 * 
 */
public class AppenderHelper {

    private static final Logger logger = Logger.getLoggerFor(AppenderHelper.class);
    private AppenderHelperCustomisationInterface customisationInterface;

    private SocketClient socketClient;
    private String sourceApplication = "<unknown source application>";

    private BlockingDeque eventsToBeDispatched = new LinkedBlockingDeque();
    private long failureDelay = 50;
    private long currentFailureDelay = failureDelay;

    private boolean useDispatchThread = true;
    private InetAddress host;
    private HeapLogger heapLogger;
    private GCWatcher gcWatcher;
    private CpuLogger cpuLogger;
    private boolean dontThrowExceptionsIfHubIsntUp = false;
    private boolean publishProcessTelemetry = false;
    private boolean publishMachineTelemetry = false;
    private int maxDispatchQueueSize = 1000;
    private int pid = -1;

    /**
     * A testability feature: this allows the test to get notifications when events are published
     * asynchronously.
     */
    private PublishingListener publishingListener = null;
    private RunnableWorkerThread dispatcherThread;
    private String telemetry;
    private TimeProvider timeProvider = null;

    private boolean gatheringCallerDetails = false;
    private boolean closing = false;
    private String channel;
    private double failureDelayMultiplier = 2;
    private long failureDelayMaximum = TimeUtils.minutes(1);
    private int discards;

    public AppenderHelper(String name, AppenderHelperCustomisationInterface ahci) {
        customisationInterface = ahci;
        socketClient = new SocketClient(name);

        // Set the connection to throw old messages away if the write side of
        // the connection starts blocking up
        socketClient.setWriteQueueOverflowPolicy(SlowSendingPolicy.discard);

        // We dont want to get events sent back to us
        socketClient.setAutoSubscribe(false);

        // Add a default connection point for lazy configurations
        socketClient.getConnector()
                    .getConnectionPointManager()
                    .setDefaultConnectionPoint(new InetSocketAddress("localhost", VLPorts.getSocketHubDefaultPort()));

        if (useDispatchThread) {
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        runDispatchLoop();
                    }
                    catch (InterruptedException e) {
                        logger.debug("Dispatch thread interupted", e);
                    }
                }
            };

            dispatcherThread = new RunnableWorkerThread("AsynchronousSocketHandler-DispatchThread", runnable);
            dispatcherThread.setDaemon(true);
            dispatcherThread.start();
        }

        try {
            host = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e1) {
            throw new RuntimeException("Failed to get local host", e1);
        }

        pid = getPID();
        GlobalLoggingParameters.getInstance().setPid(pid);
    }

    @SuppressWarnings("restriction") public final static int getPID() {
        try {
            java.lang.management.RuntimeMXBean runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();
            java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
            jvm.setAccessible(true);
            sun.management.VMManagement mgmt = (sun.management.VMManagement) jvm.get(runtime);
            java.lang.reflect.Method pidMethod = mgmt.getClass().getDeclaredMethod("getProcessId");
            pidMethod.setAccessible(true);

            return (Integer) pidMethod.invoke(mgmt);
        }
        catch (InvocationTargetException e) {
            return -1;
        }
        catch (IllegalArgumentException e) {
            return -1;
        }
        catch (IllegalAccessException e) {
            return -1;
        }
        catch (SecurityException e) {
            return -1;
        }
        catch (NoSuchFieldException e) {
            return -1;
        }
        catch (NoSuchMethodException e) {
            return -1;
        }
    }

    private void runDispatchLoop() throws InterruptedException {
        EventSnapshot snapshot = (EventSnapshot) eventsToBeDispatched.takeFirst();
        LogEvent event = snapshot.rebuildEvent();

        try {
            socketClient.send(new LogEventMessage(event));
            if (publishingListener != null) {
                publishingListener.onSuccessfullyPublished(event);
            }

            // Reset the failure delay
            currentFailureDelay = failureDelay;
        }
        catch (LoggingMessageSenderException ftse) {
            if (!closing) {
                if (publishingListener != null) {
                    publishingListener.onUnsuccessfullyPublished(event, ftse);
                }

                // Stick the event back
                eventsToBeDispatched.putFirst(snapshot);

                if (!isDontThrowExceptionsIfHubIsntUp()) {
                    logger.info("Couldnt connect to any hubs; waiting {} ms until the next connection attempt", currentFailureDelay);
                }

                // Do the failure delay sleep
                ThreadUtils.sleep(currentFailureDelay);

                currentFailureDelay *= failureDelayMultiplier;

                // Make sure it doesn't get too crazy
                currentFailureDelay = Math.min(currentFailureDelay, failureDelayMaximum);
            }
        }
    }

    public void setFailureDelayMaximum(long failureDelayMaximum) {
        this.failureDelayMaximum = failureDelayMaximum;
    }

    public void setFailureDelayMultiplier(double failureDelayMultiplier) {
        this.failureDelayMultiplier = failureDelayMultiplier;
    }

    public InetAddress getHost() {
        return host;
    }

    public int getPid() {
        return pid;
    }

    /**
     * Testing method; sends an event directly, bypassing the queue
     * 
     * @param event
     * @throws LoggingMessageSenderException
     */
    public void sendDirect(LogEvent event) throws LoggingMessageSenderException {
        socketClient.send(new LogEventMessage(event));
    }

    public void setFailureDelay(long failureDelay) {
        this.failureDelay = failureDelay;
    }

    public long getFailureDelay() {
        return failureDelay;
    }

    public void addToQueue(EventSnapshot snapshot) {
        eventsToBeDispatched.addLast(snapshot);

        if (eventsToBeDispatched.size() > maxDispatchQueueSize) {
            discards++;
            try {
                eventsToBeDispatched.removeFirst();
            }
            catch (NoSuchElementException e) {
                // jshaw - it is possible that the dispatcher thread has just sent the entire queue
                // in one go. As we aren't synchronising the queue access on the writer side we just
                // have to suck this up and move on
            }
        }
    }

    // //////////////////////////////////////////////////////////////////
    // Accessors
    // //////////////////////////////////////////////////////////////////

    public int getDiscards() {
        return discards;
    }

    public void setWriteQueueOverflowPolicy(String policy) {
        socketClient.setWriteQueueOverflowPolicy(SlowSendingPolicy.valueOf(policy));
    }

    public void addConnectionPoint(InetSocketAddress inetSocketAddress) {
        socketClient.addConnectionPoint(inetSocketAddress);
    }

    public void removeConnectionPoint(InetSocketAddress inetSocketAddress) {
        socketClient.removeConnectionPoint(inetSocketAddress);
    }

    public void setMaxDispatchQueueSize(int maxQueueSize) {
        this.maxDispatchQueueSize = maxQueueSize;
    }

    public int getMaxDispatchQueueSize() {
        return maxDispatchQueueSize;
    }

    public void setDontThrowExceptionsIfHubIsntUp(boolean dontThrowExceptionsIfHubIsntUp) {
        this.dontThrowExceptionsIfHubIsntUp = dontThrowExceptionsIfHubIsntUp;
    }

    public boolean isDontThrowExceptionsIfHubIsntUp() {
        return dontThrowExceptionsIfHubIsntUp;
    }

    public void setSourceApplication(String sourceApplication) {

        String actualValue = StringUtils.environmentReplacement(sourceApplication);

        this.sourceApplication = actualValue;

        GlobalLoggingParameters.getInstance().setApplicationName(actualValue);
    }

    public String getSourceApplication() {
        return sourceApplication;
    }

    public void setHost(String host) {
        String replacedHost = StringUtils.environmentReplacement(host);
        String[] split = replacedHost.split(":");

        String hostname = split[0];
        int port = VLPorts.getSocketHubDefaultPort();
        if (split.length > 1) {
            port = Integer.parseInt(split[1]);
        }

        GlobalLoggingParameters.getInstance().setDestination(hostname);

        addConnectionPoint(new InetSocketAddress(hostname, port));
    }

    public synchronized void setPublishMachineTelemetry(boolean publishMachineTelemetry) {
        this.publishMachineTelemetry = publishMachineTelemetry;

        throw new NotImplementedException("The telemetry bits have been removed in this version");
    }

    public synchronized void setTelemetry(String connectionString) {
        throw new NotImplementedException("The telemetry bits have been removed in this version");
    }

    public synchronized String getTelemetry() {
        return telemetry;
    }

    public synchronized void setPublishProcessTelemetry(boolean publishProcessTelemetry) {
        this.publishProcessTelemetry = publishProcessTelemetry;

        if (publishProcessTelemetry) {
            throw new NotImplementedException("The telemetry bits have been removed in this version");
        }
    }

    public boolean isPublishMachineTelemetry() {
        return publishMachineTelemetry;
    }

    public boolean isPublishProcessTelemetry() {
        return publishProcessTelemetry;
    }

    public void setForceFlush(boolean forceFlush) {
        socketClient.setForceFlush(forceFlush);
    }

    public synchronized void setHeapLogging(boolean value) {
        if (value && heapLogger == null) {
            heapLogger = customisationInterface.createHeapLogger();
            heapLogger.start();
        }
    }

    public synchronized void setCpuLogging(boolean value) {
        if (value && cpuLogger == null) {
            cpuLogger = customisationInterface.createCPULogger();
            cpuLogger.setDisplayPerThreadDetails(false);
            cpuLogger.start();
        }
    }

    public boolean isCpuLogging() {
        return cpuLogger != null;
    }

    public boolean isHeapLogging() {
        return heapLogger != null;
    }

    public boolean isGCLogging() {
        return gcWatcher != null;
    }

    public synchronized void setDetailedCpuLogging(boolean value) {
        if (value && cpuLogger == null) {
            cpuLogger = customisationInterface.createCPULogger();
            cpuLogger.setDisplayPerThreadDetails(true);
            cpuLogger.start();
        }
    }

    public void setGCLogging(String path) {
        if (gcWatcher == null) {
            gcWatcher = customisationInterface.createGCWatcher();
            try {
                gcWatcher.start(path);
            }
            catch (FileNotFoundException e) {
                logger.warn(e, "Failed to start gc watcher in path '{}'", path);
            }
        }
    }

    public void setUseDispatchThread(boolean value) {
        useDispatchThread = value;
    }

    public boolean isUseDispatchThread() {
        return useDispatchThread;
    }

    public synchronized void start() {
        
    }

    public synchronized void stop() {
        
    }

    public synchronized void close() {
        logger.debug("Closing appender helper...");
        this.closing = true;

        stop();

        if (cpuLogger != null) {
            cpuLogger.stop();
            logger.debug("Stopped CPU Logger");
        }

        if (heapLogger != null) {
            heapLogger.stop();
            logger.debug("Stopped Heap Logger");
        }

        if (socketClient != null) {
            socketClient.close();
            logger.debug("Closed socket client");
        }

        if (dispatcherThread != null) {
            dispatcherThread.stop();
            logger.debug("Stopped dispatcher thread");
        }

        if (gcWatcher != null) {
            gcWatcher.stop();
            logger.debug("Stopped gc watcher thread");
        }

        logger.debug("Closed appender helper.");
    }

    public void setPublishingListener(PublishingListener publishingListener) {
        this.publishingListener = publishingListener;
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public void setSocketClient(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    public void setChannel(String channel) {
        this.channel = StringUtils.environmentReplacement(channel);
    }

    public void flush() {
        if (socketClient != null) {
            socketClient.flush();
        }
    }

    public void waitUntilAllRecordsHaveBeenPublished() {
        boolean done = false;

        while (!done) {
            synchronized (eventsToBeDispatched) {
                done = eventsToBeDispatched.isEmpty();
            }

            if (!done) {
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {

                }
            }
        }
    }

    public void replaceConnectionList(List<InetSocketAddress> parseAddressAndPortList) {
        socketClient.replaceConnectionList(parseAddressAndPortList);
    }

    public void send(LoggingMessage logEventMessage) throws LoggingMessageSenderException {
        socketClient.send(logEventMessage);
    }

    public void setWriteQueueOverflowPolicy(SlowSendingPolicy valueOf) {
        socketClient.setWriteQueueOverflowPolicy(valueOf);
    }

    public TimeProvider getTimeProvider() {
        return timeProvider;
    }

    public void append(AppenderHelperEventConvertor appenderHelperEventConvertor) {
        if (useDispatchThread) {
            addToQueue(appenderHelperEventConvertor.createSnapshot());
        }
        else {
            LogEvent event = appenderHelperEventConvertor.createLogEvent();
            try {

                LogEventMessage message = new LogEventMessage(event);
                socketClient.send(message);
                if (publishingListener != null) {
                    publishingListener.onSuccessfullyPublished(event);
                }
            }
            catch (LoggingMessageSenderException e) {
                if (publishingListener != null) {
                    publishingListener.onUnsuccessfullyPublished(event, e);
                }

                boolean okToThrow = true;
                if (e.getCause() instanceof ConnectorException && isDontThrowExceptionsIfHubIsntUp()) {
                    // Fine, we have been told to supress these
                    okToThrow = false;
                }

                if (okToThrow) {
                    throw new RuntimeException("Failed to send log event", e);
                }
            }
        }

    }

    public BlockingDeque getEventsToBeDispatched() {
        return eventsToBeDispatched;
    }

    public boolean isGatheringCallerDetails() {
        return gatheringCallerDetails;
    }

    public void setGatheringCallerDetails(boolean gatheringCallerDetails) {
        this.gatheringCallerDetails = gatheringCallerDetails;
    }

    public String getChannel() {
        return channel;
    }

    public SocketClient getSocketClient() {
        return socketClient;
    }

}

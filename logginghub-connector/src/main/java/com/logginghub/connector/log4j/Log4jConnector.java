package com.logginghub.connector.log4j;

import java.net.InetSocketAddress;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import com.logginghub.connector.common.AppenderHelper;
import com.logginghub.connector.common.AppenderHelperCustomisationInterface;
import com.logginghub.connector.common.AppenderHelperEventConvertor;
import com.logginghub.connector.common.EventSnapshot;
import com.logginghub.connector.common.LogEvent;
import com.logginghub.connector.common.LogEventMessage;
import com.logginghub.connector.common.LoggingMessageSenderException;
import com.logginghub.connector.common.PublishingListener;
import com.logginghub.connector.common.SocketClient;
import com.logginghub.connector.common.SocketConnection.SlowSendingPolicy;
import com.logginghub.utils.CpuLogger;
import com.logginghub.utils.GCWatcher;
import com.logginghub.utils.HeapLogger;
import com.logginghub.utils.StandardAppenderFeatures;
import com.logginghub.utils.TimeProvider;

public class Log4jConnector extends AppenderSkeleton implements StandardAppenderFeatures {

    private AppenderHelper appenderHelper;

    public Log4jConnector() {
        appenderHelper = new AppenderHelper("VertexLabs-log4jSocketAppender", new AppenderHelperCustomisationInterface() {
            public HeapLogger createHeapLogger() {
                final Logger logger = Logger.getLogger("heap-logger");
                return new HeapLogger() {
                    @Override protected void log(String format) {
                        logger.debug(format);
                    }
                };
            }

            public GCWatcher createGCWatcher() {
                final Logger logger = Logger.getLogger("gc-logger");
                return new GCWatcher() {
                    @Override protected void log(String gcLine) {
                        logger.debug(gcLine);
                    }
                };
            }

            public CpuLogger createCPULogger() {
                final Logger logger = Logger.getLogger("cpu-logger");
                return new CpuLogger() {
                    @Override protected void log(String message) {
                        logger.debug(message);
                    }
                };
            }
        });
    }

    /**
     * Testing method; sends an event directly, bypassing the queue
     * 
     * @param event
     * @throws LoggingMessageSenderException
     */
    public void sendDirect(LogEvent event) throws LoggingMessageSenderException {
        appenderHelper.send(new LogEventMessage(event));
    }

    public void setFailureDelay(long failureDelay) {
        appenderHelper.setFailureDelay(failureDelay);
    }

    public long getFailureDelay() {
        return appenderHelper.getFailureDelay();
    }

    // //////////////////////////////////////////////////////////////////
    // Accessors
    // //////////////////////////////////////////////////////////////////

    public void setWriteQueueOverflowPolicy(String policy) {
        appenderHelper.setWriteQueueOverflowPolicy(SlowSendingPolicy.valueOf(policy));
    }

    public void addConnectionPoint(InetSocketAddress inetSocketAddress) {
        appenderHelper.addConnectionPoint(inetSocketAddress);
    }

    public void removeConnectionPoint(InetSocketAddress inetSocketAddress) {
        appenderHelper.removeConnectionPoint(inetSocketAddress);
    }

    public void setMaxDispatchQueueSize(int maxQueueSize) {
        appenderHelper.setMaxDispatchQueueSize(maxQueueSize);
    }

    public int getMaxDispatchQueueSize() {
        return appenderHelper.getMaxDispatchQueueSize();
    }

    public void setDontThrowExceptionsIfHubIsntUp(boolean dontThrowExceptionsIfHubIsntUp) {
        appenderHelper.setDontThrowExceptionsIfHubIsntUp(dontThrowExceptionsIfHubIsntUp);
    }

    public boolean isDontThrowExceptionsIfHubIsntUp() {
        return appenderHelper.isDontThrowExceptionsIfHubIsntUp();
    }

    public void setSourceApplication(String sourceApplication) {
        appenderHelper.setSourceApplication(sourceApplication);
    }

    public String getSourceApplication() {
        return appenderHelper.getSourceApplication();
    }

    public void setHost(String host) {
        appenderHelper.setHost(host);
    }

    public synchronized void setPublishMachineTelemetry(boolean publishMachineTelemetry) {
        appenderHelper.setPublishMachineTelemetry(publishMachineTelemetry);
    }

    public synchronized void setTelemetry(String connectionString) {
        appenderHelper.setTelemetry(connectionString);
    }

    public String getTelemetry() {
        return appenderHelper.getTelemetry();
    }

    public synchronized void setPublishProcessTelemetry(boolean publishProcessTelemetry) {
        appenderHelper.setPublishProcessTelemetry(publishProcessTelemetry);
    }

    public boolean isPublishMachineTelemetry() {
        return appenderHelper.isPublishMachineTelemetry();
    }

    public boolean isPublishProcessTelemetry() {
        return appenderHelper.isPublishProcessTelemetry();
    }

    public void setForceFlush(boolean forceFlush) {
        appenderHelper.setForceFlush(forceFlush);
    }

    public synchronized void setHeapLogging(boolean value) {
        appenderHelper.setHeapLogging(value);
    }

    public synchronized void setCpuLogging(boolean value) {
        appenderHelper.setCpuLogging(value);
    }

    public synchronized void setDetailedCpuLogging(boolean value) {
        appenderHelper.setDetailedCpuLogging(value);
    }

    public void setGCLogging(String path) {
        appenderHelper.setGCLogging(path);
    }

    public void setUseDispatchThread(boolean value) {
        appenderHelper.setUseDispatchThread(value);
        // useDispatchThread = value;
    }

    public boolean isUseDispatchThread() {
        return appenderHelper.isUseDispatchThread();
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // The log4j appender interface
    // //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override public void activateOptions() {
        super.activateOptions();

        appenderHelper.start();
    }
    
    @Override public void append(final LoggingEvent record) {

        final Log4jDetailsSnapshot details = Log4jDetailsSnapshot.fromLoggingEvent(record, appenderHelper.getTimeProvider());
        appenderHelper.append(new AppenderHelperEventConvertor() {
            public LogEvent createLogEvent() {
                Log4jLogEvent event = new Log4jLogEvent(record,
                                                        appenderHelper.getSourceApplication(),
                                                        appenderHelper.getHost(),
                                                        Thread.currentThread().getName(),
                                                        details);
                event.setPid(appenderHelper.getPid());
                event.setChannel(appenderHelper.getChannel());
                return event;
            }

            public EventSnapshot createSnapshot() {
                return new EventSnapshot() {
                    public LogEvent rebuildEvent() {
                        return createLogEvent();
                    }
                };
            }

        });
    }

    @Override public void close() {
        appenderHelper.close();
    }

    @Override public boolean requiresLayout() {
        return false;
    }

    public void setPublishingListener(PublishingListener publishingListener) {
        appenderHelper.setPublishingListener(publishingListener);
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        appenderHelper.setTimeProvider(timeProvider);
    }

    public void setSocketClient(SocketClient socketClient) {
        appenderHelper.setSocketClient(socketClient);
    }

    public void setChannel(String string) {
        appenderHelper.setChannel(string);
    }

    public void setFailureDelayMaximum(long failureDelayMaximum) {
        appenderHelper.setFailureDelayMaximum(failureDelayMaximum);
    }

    public void setFailureDelayMultiplier(double failureDelayMultiplier) {
        appenderHelper.setFailureDelayMultiplier(failureDelayMultiplier);
    }

    public void setMaximumQueuedMessages(int maximumQueuedMessages) {
        appenderHelper.setMaxDispatchQueueSize(maximumQueuedMessages);
    }

    public boolean isStackTraceModuleEnabled() {
        return appenderHelper.isStackTraceModuleEnabled();
    }

    public String getStackTraceModuleBroadcastInterval() {
        return appenderHelper.getStackTraceModuleBroadcastInterval();
    }

    public void setStackTraceModuleBroadcastInterval(String string) {
        appenderHelper.setStackTraceModuleBroadcastInterval(string);
    }

    public void setStackTraceModuleEnabled(boolean value) {
        appenderHelper.setStackTraceModuleEnabled(value);
    }
    
}

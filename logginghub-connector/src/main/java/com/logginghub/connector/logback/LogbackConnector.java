package com.logginghub.connector.logback;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import com.logginghub.connector.common.AppenderHelper;
import com.logginghub.connector.common.AppenderHelperCustomisationInterface;
import com.logginghub.connector.common.AppenderHelperEventConvertor;
import com.logginghub.connector.common.EventSnapshot;
import com.logginghub.connector.common.LogEvent;
import com.logginghub.connector.common.PublishingListener;
import com.logginghub.connector.common.SocketClient;
import com.logginghub.connector.common.SocketConnection.SlowSendingPolicy;
import com.logginghub.utils.CpuLogger;
import com.logginghub.utils.GCWatcher;
import com.logginghub.utils.HeapLogger;
import com.logginghub.utils.StandardAppenderFeatures;
import com.logginghub.utils.TimeProvider;

import edu.emory.mathcs.backport.java.util.concurrent.BlockingDeque;

public class LogbackConnector extends AppenderBase<ILoggingEvent> implements StandardAppenderFeatures {

    private AppenderHelper appenderHelper;

    public LogbackConnector() {
        appenderHelper = new AppenderHelper("VertexLabs-logbackSocketAppender", new AppenderHelperCustomisationInterface() {
            public HeapLogger createHeapLogger() {
                final Logger logger = LoggerFactory.getLogger("heap-logger");
                return new HeapLogger() {
                    @Override protected void log(String format) {
                        logger.debug(format);
                    }
                };
            }

            public GCWatcher createGCWatcher() {
                final Logger logger = LoggerFactory.getLogger("gc-logger");
                return new GCWatcher() {
                    @Override protected void log(String gcLine) {
                        logger.debug(gcLine);
                    }
                };
            }

            public CpuLogger createCPULogger() {
                final Logger logger = LoggerFactory.getLogger("cpu-logger");
                return new CpuLogger() {
                    @Override protected void log(String message) {
                        logger.debug(message);
                    }
                };
            }
        });
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
        appenderHelper.setPublishProcessTelemetry(publishMachineTelemetry);
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
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // The logback classic appender interface
    // //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override protected void append(ILoggingEvent record) {
        final LogbackDetailsSnapshot details = LogbackDetailsSnapshot.fromLoggingEvent(record, appenderHelper.getTimeProvider());
        appenderHelper.append(new AppenderHelperEventConvertor() {

            public EventSnapshot createSnapshot() {
                return new EventSnapshot() {
                    public LogEvent rebuildEvent() {
                        return createLogEvent();
                    }
                };

            }

            public LogEvent createLogEvent() {
                ILoggingEvent record = details.getLoggingEvent();
                LogbackLogEvent event = new LogbackLogEvent(record,
                                                            appenderHelper.getSourceApplication(),
                                                            appenderHelper.getHost(),
                                                            record.getThreadName(),
                                                            details);
                event.setPid(appenderHelper.getPid());
                event.setChannel(appenderHelper.getChannel());
                return event;
            }
        });
    }

    @Override public void start() {
        super.start();
        appenderHelper.start();
    }

    @Override public void stop() {
        super.stop();
        appenderHelper.close();
    }

    public void setPublishingListener(PublishingListener publishingListener) {
        appenderHelper.setPublishingListener(publishingListener);
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        appenderHelper.setTimeProvider(timeProvider);
    }

    public void setChannel(String channel) {
        appenderHelper.setChannel(channel);
    }

    public String getChannel() {
        return appenderHelper.getChannel();
    }

    /**
     * For testing purposes only.
     * 
     * @param socketClient
     */
    public void setSocketClient(SocketClient socketClient) {
        appenderHelper.setSocketClient(socketClient);
    }

    public BlockingDeque getEventsToBeDispatched() {
        return appenderHelper.getEventsToBeDispatched();
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

    public void setGCLogging(boolean value) {
        appenderHelper.setGCLogging(name);
    }

    public AppenderHelper getAppenderHelper() {
        return appenderHelper;
    }
}

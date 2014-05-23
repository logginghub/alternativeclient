package com.logginghub.connector.jul;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.logginghub.connector.common.AppenderHelper;
import com.logginghub.connector.common.AppenderHelperCustomisationInterface;
import com.logginghub.connector.common.AppenderHelperEventConvertor;
import com.logginghub.connector.common.EventSnapshot;
import com.logginghub.connector.common.LogEvent;
import com.logginghub.utils.CpuLogger;
import com.logginghub.utils.GCWatcher;
import com.logginghub.utils.HeapLogger;
import com.logginghub.utils.LoggingUtils;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.StandardAppenderFeatures;
import com.logginghub.utils.VLPorts;

/**
 * Provides the basic socket handler mechanisms for the sync and async handlers.
 * 
 * @author admin
 * 
 */
public class JULConnector extends Handler implements PropertyChangeListener, StandardAppenderFeatures {

    private AppenderHelper appenderHelper;

    public JULConnector() {
        this("");
    }

    public JULConnector(String name) {
        appenderHelper = new AppenderHelper(name, new AppenderHelperCustomisationInterface() {
            public HeapLogger createHeapLogger() {
                final Logger logger = Logger.getLogger("heap-logger");
                return new HeapLogger() {
                    @Override protected void log(String format) {
                        logger.fine(format);
                    }
                };
            }

            public GCWatcher createGCWatcher() {
                final Logger logger = Logger.getLogger("gc-logger");
                return new GCWatcher() {
                    @Override protected void log(String gcLine) {
                        logger.fine(gcLine);
                    }
                };
            }

            public CpuLogger createCPULogger() {
                final Logger logger = Logger.getLogger("cpu-logger");
                return new CpuLogger() {
                    @Override protected void log(String message) {
                        logger.fine(message);
                    }
                };
            }
        });

        LogManager manager = LogManager.getLogManager();
        manager.addPropertyChangeListener(this);
        reconfigure();
    }

    // //////////////////////////////////////////////////////////////////
    // Accessors
    // //////////////////////////////////////////////////////////////////

    public void addConnectionPoint(InetSocketAddress inetSocketAddress) {
        appenderHelper.addConnectionPoint(inetSocketAddress);
    }

    public void removeConnectionPoint(InetSocketAddress inetSocketAddress) {
        appenderHelper.removeConnectionPoint(inetSocketAddress);
    }

    public void setSourceApplication(String sourceApplication) {
        appenderHelper.setSourceApplication(sourceApplication);
    }

    public String getSourceApplication() {
        return appenderHelper.getSourceApplication();
    }

    // //////////////////////////////////////////////////////////////////
    // Handler overrides
    // //////////////////////////////////////////////////////////////////

    @Override public void close() throws SecurityException {
        appenderHelper.close();
        
        LogManager manager = LogManager.getLogManager();
        manager.removePropertyChangeListener(this);
    }

    @Override public void flush() {
        appenderHelper.flush();
    }

    @Override public void publish(final LogRecord record) {

        if (appenderHelper.isGatheringCallerDetails()) {
            // Forces lazy gathering of caller details
            record.getSourceClassName();
        }
        final JULDetailsSnapshot snapshot = JULDetailsSnapshot.fromLoggingEvent(record, appenderHelper.getTimeProvider());

        appenderHelper.append(new AppenderHelperEventConvertor() {

            public EventSnapshot createSnapshot() {
                return new EventSnapshot() {
                    public LogEvent rebuildEvent() {
                        return createLogEvent();
                    }
                };
            }

            public LogEvent createLogEvent() {
                JuliLogEvent event = new JuliLogEvent(record,
                                                      appenderHelper.getSourceApplication(),
                                                      appenderHelper.getHost(),
                                                      snapshot.getThreadName(),
                                                      appenderHelper.isGatheringCallerDetails());
                event.setPid(appenderHelper.getPid());
                event.setChannel(appenderHelper.getChannel());
                return event;
            }
        });
    }

    // //////////////////////////////////////////////////////////////////
    // The LogManager property change implementation
    // //////////////////////////////////////////////////////////////////

    public void propertyChange(PropertyChangeEvent evt) {
        reconfigure();
    }

    // //////////////////////////////////////////////////////////////////
    // Private methods
    // //////////////////////////////////////////////////////////////////

    /*
     * private LogEvent getEventForThread() { LogEvent fullLogEvent = m_logEventsByThread.get();
     * 
     * if(fullLogEvent == null) { fullLogEvent = new LogEvent();
     * m_logEventsByThread.set(fullLogEvent); }
     * 
     * return fullLogEvent; }
     */

    private void reconfigure() {
        appenderHelper.stop();
        
        LogManager manager = LogManager.getLogManager();

        String cname = getClass().getName();

        Level level = LoggingUtils.getLevelProperty(cname + ".level");
        if (level != null) {
            setLevel(level);
        }

        String applicationNameProperty = manager.getProperty(cname + ".applicationName");
        if (applicationNameProperty != null) {
            setSourceApplication(applicationNameProperty);
        }

        String property = manager.getProperty(cname + ".connectionPoints");
        if (property != null) {
            List<InetSocketAddress> parseAddressAndPortList = NetUtils.toInetSocketAddressList(property, VLPorts.getSocketHubDefaultPort());
            appenderHelper.replaceConnectionList(parseAddressAndPortList);
        }
        
        // This is the log4j style way of setting connection points, it'll compliment the other setting rather than override it
        String host = manager.getProperty(cname + ".host");
        if (host != null) {
            setHost(host);
        }
        
        String forceFlushProperty = manager.getProperty(cname + ".forceFlush");
        if (forceFlushProperty != null) {
            boolean value = Boolean.parseBoolean(forceFlushProperty);
            setForceFlush(value);
        }

        String publishProcessTelemetry = manager.getProperty(cname + ".publishProcessTelemetry");
        if (publishProcessTelemetry != null) {
            setPublishProcessTelemetry(Boolean.parseBoolean(publishProcessTelemetry));
        }

        String publishMachineTelemetry = manager.getProperty(cname + ".publishMachineTelemetry");
        if (publishMachineTelemetry != null) {
            setPublishMachineTelemetry(Boolean.parseBoolean(publishMachineTelemetry));
        }

        String useDispatchThread = manager.getProperty(cname + ".useDispatchThread");
        if (useDispatchThread != null) {
            setUseDispatchThread(Boolean.parseBoolean(useDispatchThread));
        }
        
        String telemetry = manager.getProperty(cname + ".telemetry");
        if (telemetry != null) {
            setTelemetry(telemetry);
        }

        String channel = manager.getProperty(cname + ".channel");
        if (channel != null) {
            appenderHelper.setChannel(channel);
        }
        
        String stackTraceModuleBroadcastInterval = manager.getProperty(cname + ".stackTraceModuleBroadcastInterval");
        if(stackTraceModuleBroadcastInterval != null) {
            setStackTraceModuleBroadcastInterval(stackTraceModuleBroadcastInterval);
        }
        
        String stackTraceModuleEnabled = manager.getProperty(cname + ".stackTraceModuleEnabled");
        if(stackTraceModuleEnabled != null) {
            setStackTraceModuleEnabled(Boolean.parseBoolean(stackTraceModuleEnabled));
        }

        String gatherCallerDetails = manager.getProperty(cname + ".gatherCallerDetails");
        if (gatherCallerDetails != null) {
            setGatheringCallerDetails(Boolean.parseBoolean(gatherCallerDetails));
        }

        String failureDelay = manager.getProperty(cname + ".failureDelayMaximum");
        String failureDelayMaximum = manager.getProperty(cname + ".failureDelayMaximum");
        String failureDelayMultiplier = manager.getProperty(cname + ".failureDelayMultiplier");
        String writeQueueOverflowPolicy = manager.getProperty(cname + ".writeQueueOverflowPolicy");
        String cpuLogging = manager.getProperty(cname + ".cpuLogging");
        String gcLogging = manager.getProperty(cname + ".gcLogging");
        String heapLogging = manager.getProperty(cname + ".heapLogging");
        String maximumQueuedMessages = manager.getProperty(cname + ".maximumQueuedMessages");
        String dontThrowExceptionsIfHubIsntUp = manager.getProperty(cname + ".dontThrowExceptionsIfHubIsntUp");

        if(maximumQueuedMessages != null) {
            setMaximumQueuedMessages(Integer.parseInt(maximumQueuedMessages));
        }
        
        if(failureDelay != null){
            setFailureDelay(Long.parseLong(failureDelay));
        }
        
        if(failureDelayMaximum != null){
            setFailureDelayMaximum(Long.parseLong(failureDelayMaximum));
        }
        
        if(failureDelayMultiplier != null){
            setFailureDelayMultiplier(Long.parseLong(failureDelayMultiplier));
        }
        
        if(writeQueueOverflowPolicy != null){
            setWriteQueueOverflowPolicy(writeQueueOverflowPolicy);
        }
        
        if(cpuLogging != null) {
            setCpuLogging(Boolean.parseBoolean(cpuLogging));
        }
        
        if(dontThrowExceptionsIfHubIsntUp != null) {
            setDontThrowExceptionsIfHubIsntUp(Boolean.parseBoolean(dontThrowExceptionsIfHubIsntUp));
        }
        
        if(gcLogging != null) {
            setGCLogging(gcLogging);
        }
        
        if(heapLogging != null) {
            setHeapLogging(Boolean.parseBoolean(heapLogging));
        }
        
        appenderHelper.start();        
    }

    // //////////////////////////////////////////////////////////////////
    // Protected methods
    // //////////////////////////////////////////////////////////////////

    /**
     * @return the publisher instance.
     */
    // private SocketPublisher getPublisher()
    // {
    // return m_publisher;
    // }
    /**
     * Convert a LogRecord into a LogEvent, uses a thread local so its nice and fast
     * 
     * @param record
     * @return
     */
    /*
     * private LogEvent convert(LogRecord record) { LogEvent eventForThread = getEventForThread();
     * eventForThread.populateFromLogRecord(record, m_sourceApplication);
     * eventForThread.setThreadName(m_threadNames.remove(record)); return eventForThread; }
     */

    public void waitUntilAllRecordsHaveBeenPublished() {
        appenderHelper.waitUntilAllRecordsHaveBeenPublished();
    }

    public void setUseDispatchThread(boolean value) {
        appenderHelper.setUseDispatchThread(value);
    }

//    public void publish(TelemetryStack telemetryStackForThread) throws LoggingMessageSenderException {
//        appenderHelper.publish(telemetryStackForThread);
//    }

    public void setForceFlush(boolean b) {
        appenderHelper.setForceFlush(b);
    }

    public synchronized void setPublishMachineTelemetry(boolean publishMachineTelemetry) {
        appenderHelper.setPublishMachineTelemetry(publishMachineTelemetry);
    }

    public synchronized void setTelemetry(String connectionString) {
        appenderHelper.setTelemetry(connectionString);
    }

//    public String getTelemetry() {
//        return appenderHelper.getTelemetry();
//    }

    public synchronized void setPublishProcessTelemetry(boolean publishProcessTelemetry) {
        appenderHelper.setPublishProcessTelemetry(publishProcessTelemetry);
    }

    public void setDontThrowExceptionsIfHubIsntUp(boolean dontThrowExceptionsIfHubIsntUp) {
        appenderHelper.setDontThrowExceptionsIfHubIsntUp(dontThrowExceptionsIfHubIsntUp);
    }

    public void setMaximumQueuedMessages(int maximumQueuedMessages) {
        appenderHelper.setMaxDispatchQueueSize(maximumQueuedMessages);
    }

    public void setGatheringCallerDetails(boolean gatheringCallerDetails) {
        appenderHelper.setGatheringCallerDetails(gatheringCallerDetails);
    }

//    public boolean isGatheringCallerDetails() {
//        return appenderHelper.isGatheringCallerDetails();
//    }

    public AppenderHelper getAppenderHelper() {
        return appenderHelper;
    }

    public void setFailureDelayMaximum(long failureDelayMaximum) {
        appenderHelper.setFailureDelayMaximum(failureDelayMaximum);
    }

    public void setFailureDelayMultiplier(double failureDelayMultiplier) {
        appenderHelper.setFailureDelayMultiplier(failureDelayMultiplier);
    }

    public void setWriteQueueOverflowPolicy(String policy) {
        appenderHelper.setWriteQueueOverflowPolicy(policy);
    }

    public void setHost(String host) {
        appenderHelper.setHost(host);
    }

    public void setCpuLogging(boolean value) {
        appenderHelper.setCpuLogging(value);
    }

    public void setGCLogging(String path) {
        appenderHelper.setGCLogging(path);
    }

    public void setHeapLogging(boolean value) {
        appenderHelper.setHeapLogging(value);
    }

    public void setFailureDelay(long failureDelay) {
        appenderHelper.setFailureDelay(failureDelay);
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

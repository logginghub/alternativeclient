package com.logginghub.modules;

import java.util.concurrent.TimeUnit;

import com.logginghub.connector.common.Channels;
import com.logginghub.connector.common.LoggingMessageSender;
import com.logginghub.connector.common.LoggingMessageSenderException;
import com.logginghub.connector.common.messages.ChannelMessage;
import com.logginghub.utils.Logger;
import com.logginghub.utils.StackCapture;
import com.logginghub.utils.StackSnapshot;
import com.logginghub.utils.StackStrobeRequest;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.WorkerThread;

public class StackCaptureController {

    private static final Logger logger = Logger.getLoggerFor(StackCaptureController.class);
    private WorkerThread timer;
    private StackCapture capture = new StackCapture();
    private LoggingMessageSender loggingMessageSender;
    private int instanceNumber;
    private String instanceType;
    private String host;
    private String environment;
    private long snapshotInterval;

    public StackCaptureController() {
    }

    public void configure(LoggingMessageSender loggingMessageSender,
                          long snapshotInterval,
                          String environment,
                          String host,
                          String instanceType,
                          int instanceNumber) {
        this.loggingMessageSender = loggingMessageSender;
        this.snapshotInterval = snapshotInterval;
        this.environment = environment;
        this.host = host;
        this.instanceType = instanceType;
        this.instanceNumber = instanceNumber;
    }

    public synchronized void start() {
        stop();
        if (snapshotInterval > 0) {
            timer = WorkerThread.every("LoggingHub-stackCaptureThread", snapshotInterval, TimeUnit.MILLISECONDS, new Runnable() {
                public void run() {
                    takeSnapshot();
                }
            });
        }

    }

    public synchronized void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        else {
        }
    }

    protected void takeSnapshot() {
        StackSnapshot snapshot = capture.capture(environment, host, instanceType, instanceNumber);

        ChannelMessage channelMessage = new ChannelMessage(Channels.stackSnapshots, snapshot);
        
        try {
            loggingMessageSender.send(channelMessage);
        }
        catch (LoggingMessageSenderException e) {
            // TODO : is this is a connection exception is ok, anything else is not ok
            // TODO : add a proper disconnection exception into the heiarchy to detect this
            // logger.warn(e, "Failed to send stack snapshot");
        }

    }

    public void executeStrobe(final StackStrobeRequest request) {
        WorkerThread.executeDaemon("LoggingHub-strobeExecutor", new Runnable() {
            public void run() {
                int snapshotCount = request.getSnapshotCount();
                if (snapshotCount > 0) {
                    long intervalLength = request.getIntervalLength();
                    long sleepDuration = intervalLength / snapshotCount;
                    for (int i = 0; i < snapshotCount; i++) {
                        takeSnapshot();
                        ThreadUtils.sleep(sleepDuration);
                    }
                }
                else {
                    takeSnapshot();
                }

                logger.info("Strobe '{}' complete", request);
            }
        });

    }

}

package com.logginghub.connector.common;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;

import com.logginghub.connector.jul.JULLogRecordFactory;

public class LogEventFactory {
    private static final String TEST_APPLICATION = "TestApplication";

    private static InetAddress localhost;
    static {
        try {
            localhost = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e) {
            throw new RuntimeException("Failed to resolve local host address", e);
        }
    }

    public static DefaultLogEvent createFullLogEvent1() {
        return createFullLogEvent1(TEST_APPLICATION);
    }

    public static DefaultLogEvent createFullLogEvent2() {
        return createFullLogEvent2(TEST_APPLICATION);
    }

    public static DefaultLogEvent createFullLogEvent3() {
        return createFullLogEvent3(TEST_APPLICATION);
    }

    public static DefaultLogEvent createFullLogEvent1(String sourceApplication) {
        DefaultLogEvent event = new DefaultLogEvent();
        event.populateFromLogRecord(JULLogRecordFactory.getLogRecord1(), sourceApplication, localhost);
        return event;
    }

    public static DefaultLogEvent createFullLogEvent2(String sourceApplication) {
        DefaultLogEvent event = new DefaultLogEvent();
        event.populateFromLogRecord(JULLogRecordFactory.getLogRecord2(), sourceApplication, localhost);
        return event;
    }

    public static DefaultLogEvent createFullLogEvent3(String sourceApplication) {
        DefaultLogEvent event = new DefaultLogEvent();
        event.populateFromLogRecord(JULLogRecordFactory.getLogRecord3(), sourceApplication, localhost);
        return event;
    }

    /**
     * Returns a event with a message > 64k
     * 
     * @param sourceApplication
     * @return
     */
    public static DefaultLogEvent createFullLogEventMassive(String sourceApplication) {
        DefaultLogEvent event = new DefaultLogEvent();
        event.populateFromLogRecord(JULLogRecordFactory.getLogRecordMassive(), sourceApplication, localhost);
        return event;
    }

    /**
     * Returns a event with a message > 10k
     * 
     * @param sourceApplication
     * @return
     */
    public static DefaultLogEvent createFullLogEventBig(String sourceApplication) {
        DefaultLogEvent event = new DefaultLogEvent();
        event.populateFromLogRecord(JULLogRecordFactory.getLogRecordBig(), sourceApplication, localhost);
        return event;
    }

    public static DefaultLogEvent createFullLogEventMassive() {
        return createFullLogEventMassive(TEST_APPLICATION);
    }

    public static DefaultLogEvent createRandomEvent() {

        double random = Math.random();
        int percent = (int) (random * 1000);

        Level level = null;

        if (percent >= 998) {
            level = Level.SEVERE;
        }
        else if (percent > 990) {
            level = Level.WARNING;
        }
        else if (percent > 200) {
            level = Level.INFO;
        }
        else if (percent > 100) {
            level = Level.FINE;
        }
        else if (percent >50 ) {
            level = Level.FINER;
        }
        else {
            level = Level.FINEST;
        }

        DefaultLogEvent logEvent = LogEventBuilder.start().setLevel(level.intValue()).setMessage("Random event").setSourceApplication("Source applcation").toLogEvent();        
        return logEvent;

    }

    public static DefaultLogEvent createLogEvent(String message) {
        DefaultLogEvent event = createFullLogEvent1();
        event.setMessage(message);
        return event;
         
    }

    public static DefaultLogEvent createLogEvent(long time, String message) {
        DefaultLogEvent event = createFullLogEvent1();
        event.setMessage(message);
        event.setLocalCreationTimeMillis(time);
        return event;
    }
}

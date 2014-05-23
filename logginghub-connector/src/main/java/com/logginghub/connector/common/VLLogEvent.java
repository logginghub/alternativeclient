package com.logginghub.connector.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.logging.Level;

import com.logginghub.utils.StringUtils;

/**
 * LogEvent wrapper for the com.vertexlabs.util.logging log record object.
 * 
 * @author admin
 */
public class VLLogEvent extends BaseLogEvent {

    private com.logginghub.utils.LogEvent event;
    private static String lineSeparator = StringUtils.newline;

    public VLLogEvent(com.logginghub.utils.LogEvent event, int pid, String sourceApplication, InetAddress sourceHost) {
        this.event = event;
        setSourceApplication(sourceApplication);
        setSourceAddress(sourceHost.getHostAddress());
        setSourceHost(sourceHost.getHostName());
        setPid(pid);
    }

    public int getLevel() {
        return event.getLevel();
    }

    public long getSequenceNumber() {
        return event.getSequenceNumber();
    }

    public String getSourceClassName() {
        return event.getSourceClassName();
    }

    public String getSourceMethodName() {
        return event.getSourceMethodName();
    }

    public String getMessage() {
        return event.getMessage();
    }

    public String getThreadName() {
        return event.getThreadName();
    }

    public long getLocalCreationTimeMillis() {
        return event.getLocalCreationTimeMillis();
    }

    public String getLoggerName() {
        return "?";

    }

    public String getFormattedException() {
        String formatted;

        Throwable thrown = event.getThrowable();
        if (thrown != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            sw.append(thrown.getMessage());
            sw.append(lineSeparator);
            thrown.printStackTrace(pw);
            formatted = sw.toString();
        }
        else {
            formatted = null;
        }

        return formatted;
    }

    public String[] getFormattedObject() {
        return null;
    }

    public String getFlavour() {
        return "vllogging";

    }

    public String getLevelDescription() {
        return Level.parse("" + event.getLevel()).getName();
    }

    public Level getJavaLevel() {
        return Level.parse("" + event.getLevel());
    }

}

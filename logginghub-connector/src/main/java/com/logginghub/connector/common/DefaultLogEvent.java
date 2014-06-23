package com.logginghub.connector.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofReader;
import com.logginghub.sof.SofWriter;
import com.logginghub.utils.Logger;
import com.logginghub.utils.Metadata;
import com.logginghub.utils.TimeProvider;

/**
 * The main model that represents a single item of logging.
 * 
 * @author admin
 */
public class DefaultLogEvent implements LogEvent, SerialisableObject, TimeProvider {
    private static String lineSeparator = (String) System.getProperty("line.separator");

    private int level;
    private long sequenceNumber;
    private String sourceClassName;
    private String sourceMethodName;
    private String message;
    private String threadName;
    private long localCreationTimeMillis;
    private String loggerName;
    private String sourceHost;
    private String sourceAddress;
    private String sourceApplication;
    private String channel;

    private int pid;

    private String formattedException;
    private String[] formattedObject;

    private Metadata metadata;

    public void populateFromLogRecord(LogRecord record, String sourceApplication) {
        populateFromLogRecord(record, sourceApplication, null);
    }

    public void populateFromLogRecord(LogRecord record, String sourceApplication, InetAddress sourceAddress) {
        level = record.getLevel().intValue();
        sequenceNumber = record.getSequenceNumber();
        sourceClassName = record.getSourceClassName();
        sourceMethodName = record.getSourceMethodName();
        message = record.getMessage();
        threadName = Thread.currentThread().getName();

        localCreationTimeMillis = record.getMillis();
        loggerName = record.getLoggerName();

        if (sourceAddress == null) {
            try {
                sourceAddress = InetAddress.getLocalHost();
            }
            catch (UnknownHostException e) {
                throw new RuntimeException("Failed to resolve local host address", e);
            }
        }

        this.sourceAddress = sourceAddress.getHostAddress();
        this.sourceHost = sourceAddress.getHostName();

        this.sourceApplication = sourceApplication;
        Throwable thrown = record.getThrown();
        if (thrown != null) {
            formattedException = formatException(thrown);
        }
        else {
            formattedException = null;
        }

        Object[] parameters = record.getParameters();
        if (parameters != null) {
            formattedObject = formatObjects(parameters);
        }
    }

    @Override public String toString() {
        int maxMessageDump = 400;
        if (message.length() > maxMessageDump) {
            return "[LogEvent message [" + Logger.toDateString(localCreationTimeMillis) + "] ='" + message.substring(0, maxMessageDump) + "...']";
        }
        else {
            return "[LogEvent message [" + Logger.toDateString(localCreationTimeMillis) + "] ='" + getMessage() + "']";
        }
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public void setSourceClassName(String sourceClassName) {
        this.sourceClassName = sourceClassName;
    }

    public void setSourceMethodName(String sourceMethodName) {
        this.sourceMethodName = sourceMethodName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public void setLocalCreationTimeMillis(long localCreationTimeMillis) {
        this.localCreationTimeMillis = localCreationTimeMillis;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost;
    }

    public void setSourceApplication(String sourceApplication) {
        this.sourceApplication = sourceApplication;
    }

    public void setFormattedException(String formattedException) {
        this.formattedException = formattedException;
    }

    public void setFormattedObject(String[] formattedObject) {
        this.formattedObject = formattedObject;
    }

    private String[] formatObjects(Object[] parameters) {
        String[] formattedObjects = new String[parameters.length];

        int i = 0;
        for (Object object : parameters) {
            formattedObjects[i++] = object.toString();
        }

        return formattedObjects;
    }

    public static String formatException(Throwable thrown) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        sw.append(thrown.getMessage());
        sw.append(lineSeparator);
        // NOSONAR
        thrown.printStackTrace(pw);
        // NOSONAR        
        return sw.toString();
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public int getLevel() {
        return level;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public String getSourceClassName() {
        return sourceClassName;
    }

    public String getSourceMethodName() {
        return sourceMethodName;
    }

    public String getMessage() {
        return message;
    }

    public String getThreadName() {
        return threadName;
    }

    public long getLocalCreationTimeMillis() {
        return localCreationTimeMillis;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public String getSourceApplication() {
        return sourceApplication;
    }

    public String getFormattedException() {
        return formattedException;
    }

    public String[] getFormattedObject() {
        return formattedObject;
    }

    public java.util.logging.Level getJavaLevel() {
        Level level = null;

        int levelValue = getLevel();

        if (levelValue == Level.INFO.intValue()) {
            level = Level.INFO;
        }
        else if (levelValue == Level.WARNING.intValue()) {
            level = Level.WARNING;
        }
        else if (levelValue == Level.SEVERE.intValue()) {
            level = Level.SEVERE;
        }
        else if (levelValue == Level.SEVERE.intValue()) {
            level = Level.SEVERE;
        }
        else if (levelValue == Level.CONFIG.intValue()) {
            level = Level.CONFIG;
        }
        else if (levelValue == Level.FINE.intValue()) {
            level = Level.FINE;
        }
        else if (levelValue == Level.FINER.intValue()) {
            level = Level.FINER;
        }
        else if (levelValue == Level.FINEST.intValue()) {
            level = Level.FINEST;
        }
        else if (levelValue == Level.OFF.intValue()) {
            level = Level.OFF;
        }
        else {
            level = null;
        }

        return level;
    }

    /**
     * A version of populate that doesn't force you to specific a source app. You just need to hope
     * someone further down the chain will set it for you...
     * 
     * @param lr
     */
    public void populateFromLogRecord(LogRecord lr) {
        populateFromLogRecord(lr, null, null);
    }

    /**
     * Return the java.util.Logging style level description
     * 
     * @return
     */
    public String getJULILevelDescription() {
        Level javaLevel = getJavaLevel();
        if (javaLevel != null) {
            return javaLevel.toString();
        }
        else {
            return String.format("Unknown level, int value [%d]", getLevel());
        }
    }

    public String getLevelDescription() {
        return getJULILevelDescription();
    }

    public String getFlavour() {
        return "DefaultImpl";
    }

    public void setSourceAddress(String decodeString) {
        this.sourceAddress = decodeString;
    }

    public synchronized Metadata getMetadata() {
        if (metadata == null) {
            metadata = new Metadata();
        }
        return metadata;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((formattedException == null) ? 0 : formattedException.hashCode());
        result = prime * result + Arrays.hashCode(formattedObject);
        result = prime * result + level;
        result = prime * result + (int) (localCreationTimeMillis ^ (localCreationTimeMillis >>> 32));
        result = prime * result + ((loggerName == null) ? 0 : loggerName.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
        result = prime * result + pid;
        result = prime * result + (int) (sequenceNumber ^ (sequenceNumber >>> 32));
        result = prime * result + ((sourceAddress == null) ? 0 : sourceAddress.hashCode());
        result = prime * result + ((sourceApplication == null) ? 0 : sourceApplication.hashCode());
        result = prime * result + ((sourceClassName == null) ? 0 : sourceClassName.hashCode());
        result = prime * result + ((sourceHost == null) ? 0 : sourceHost.hashCode());
        result = prime * result + ((sourceMethodName == null) ? 0 : sourceMethodName.hashCode());
        result = prime * result + ((threadName == null) ? 0 : threadName.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DefaultLogEvent other = (DefaultLogEvent) obj;
        if (formattedException == null) {
            if (other.formattedException != null) return false;
        }
        else if (!formattedException.equals(other.formattedException)) {
            return false;
        }
        if (!Arrays.equals(formattedObject, other.formattedObject)) {
            return false;
        }
        if (level != other.level) {
            return false;
        }
        if (localCreationTimeMillis != other.localCreationTimeMillis) {
            return false;
        }
        if (loggerName == null) {
            if (other.loggerName != null) {
                return false;
            }
        }
        else if (!loggerName.equals(other.loggerName)) {
            return false;
        }
        if (message == null) {
            if (other.message != null) {
                return false;
            }
        }
        else if (!message.equals(other.message)) {
            return false;
        }
        if (metadata == null) {
            if (other.metadata != null) {
                return false;
            }
        }
        else if (!metadata.equals(other.metadata)) {
            return false;
        }
        if (pid != other.pid) {
            return false;
        }
        if (sequenceNumber != other.sequenceNumber) {
            return false;
        }
        if (sourceAddress == null) {
            if (other.sourceAddress != null) {
                return false;
            }
        }
        else if (!sourceAddress.equals(other.sourceAddress)) {
            return false;
        }
        if (sourceApplication == null) {
            if (other.sourceApplication != null) {
                return false;
            }
        }
        else if (!sourceApplication.equals(other.sourceApplication)) {
            return false;
        }
        if (sourceClassName == null) {
            if (other.sourceClassName != null) {
                return false;
            }
        }
        else if (!sourceClassName.equals(other.sourceClassName)) {
            return false;
        }
        if (sourceHost == null) {
            if (other.sourceHost != null) {
                return false;
            }
        }
        else if (!sourceHost.equals(other.sourceHost)) {
            return false;
        }
        if (sourceMethodName == null) {
            if (other.sourceMethodName != null) {
                return false;
            }
        }
        else if (!sourceMethodName.equals(other.sourceMethodName)) {
            return false;
        }
        if (threadName == null) {
            if (other.threadName != null) {
                return false;
            }
        }
        else if (!threadName.equals(other.threadName)) {
            return false;
        }
        return true;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getPid() {
        return pid;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void read(SofReader reader) throws SofException {
        localCreationTimeMillis = reader.readLong(1);
        level = reader.readInt(2);
        message = reader.readString(3);
        sourceHost = reader.readString(4);
        sourceApplication = reader.readString(5);
        pid = reader.readInt(6);
        threadName = reader.readString(7);
        loggerName = reader.readString(8);
        sourceAddress = reader.readString(9);
        channel = reader.readString(10);
        sourceClassName = reader.readString(11);
        sourceMethodName = reader.readString(12);
        formattedException = reader.readString(13);
        formattedObject = reader.readStringArray(14);
        sequenceNumber = reader.readLong(15);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, localCreationTimeMillis);
        writer.write(2, level);
        writer.write(3, message);
        writer.write(4, sourceHost);
        writer.write(5, sourceApplication);
        writer.write(6, pid);
        writer.write(7, threadName);
        writer.write(8, loggerName);
        writer.write(9, sourceAddress);
        writer.write(10, channel);
        writer.write(11, sourceClassName);
        writer.write(12, sourceMethodName);
        writer.write(13, formattedException);
        writer.write(14, formattedObject);
        writer.write(15, sequenceNumber);
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public static int estimateSizeOf(String string) {

        int sizeof = 0;

        if (string == null) {
            sizeof += 8;
        }
        else {
            sizeof += 8; // object reference
            sizeof += 12; // string int fields
            sizeof += 4; // array object reference
            sizeof += 14 + string.length() * 2; // actual bytes
        }

        return sizeof;

    }

    public int estimateSizeOf() {

        // time + sequence + level + pid + (12 * reference)
        int sizeof = 62;

        int nonNullStrings = 0;
        int lengths = 0;

        if (message != null) {
            nonNullStrings++;
            lengths += message.length();
        }

        if (sourceHost != null) {
            nonNullStrings++;
            lengths += sourceHost.length();
        }

        if (sourceApplication != null) {
            nonNullStrings++;
            lengths += sourceApplication.length();
        }

        if (threadName != null) {
            nonNullStrings++;
            lengths += threadName.length();
        }

        if (loggerName != null) {
            nonNullStrings++;
            lengths += loggerName.length();
        }

        if (sourceAddress != null) {
            nonNullStrings++;
            lengths += sourceAddress.length();
        }

        if (channel != null) {
            nonNullStrings++;
            lengths += channel.length();
        }

        if (sourceClassName != null) {
            nonNullStrings++;
            lengths += sourceClassName.length();
        }

        if (sourceMethodName != null) {
            nonNullStrings++;
            lengths += sourceMethodName.length();
        }

        if (formattedException != null) {
            nonNullStrings++;
            lengths += formattedException.length();
        }

        sizeof += (nonNullStrings * 36) + (lengths * 2);

        return sizeof;
    }

    public long getTime() {
        return getLocalCreationTimeMillis();
    }

}

package com.logginghub.utils;

public class LoggerEvent {
    private int level;
    private long sequenceNumber;
    private String sourceClassName;
    private String sourceMethodName;
    private String message;
    private String threadName;
    private long localCreationTimeMillis;
    private Throwable throwable;
    private String threadContext;
    private int patternID = -1;
    private String[] parameters;

    public void setThreadContext(String threadContext) {
        this.threadContext = threadContext;
    }

    public String getThreadContext() {
        return threadContext;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getSourceClassName() {
        return sourceClassName;
    }

    public void setSourceClassName(String sourceClassName) {
        this.sourceClassName = sourceClassName;
    }

    public String getSourceMethodName() {
        return sourceMethodName;
    }

    public void setSourceMethodName(String sourceMethodName) {
        this.sourceMethodName = sourceMethodName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public long getLocalCreationTimeMillis() {
        return localCreationTimeMillis;
    }

    public void setLocalCreationTimeMillis(long localCreationTimeMillis) {
        this.localCreationTimeMillis = localCreationTimeMillis;
    }

    public void setPatternID(int patternID) {
        this.patternID = patternID;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }

    public String[] getParameters() {
        return parameters;
    }

    public int getPatternID() {
        return patternID;
    }

}

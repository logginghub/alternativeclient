package com.logginghub.connector.common.messages;

import java.io.Serializable;

import com.logginghub.connector.common.LoggingMessage;

public class FilterRequestMessage implements LoggingMessage, Serializable {
    private static final long serialVersionUID = 1L;
    private int levelFilter;

    public FilterRequestMessage(int levelFilter) {
        this.levelFilter = levelFilter;
    }
    
    public int getLevelFilter() {
        return levelFilter;
    }

    @Override public String toString() {
        return "FilterRequestMessage [levelFilter=" + levelFilter + "]";
    }
}

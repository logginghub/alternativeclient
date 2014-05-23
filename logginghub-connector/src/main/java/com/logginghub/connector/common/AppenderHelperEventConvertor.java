package com.logginghub.connector.common;


public interface AppenderHelperEventConvertor {
    LogEvent createLogEvent();
    EventSnapshot createSnapshot();
}

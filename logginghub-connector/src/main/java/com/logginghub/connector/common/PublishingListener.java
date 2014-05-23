package com.logginghub.connector.common;

public interface PublishingListener {
    public void onSuccessfullyPublished(LogEvent event);

    public void onUnsuccessfullyPublished(LogEvent event, Exception failureReason);
}

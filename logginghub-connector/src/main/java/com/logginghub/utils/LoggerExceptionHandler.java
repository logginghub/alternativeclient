package com.logginghub.utils;


public class LoggerExceptionHandler implements ExceptionHandler {

    private static final Logger logger = Logger.getLoggerFor(LoggerExceptionHandler.class);

    public void handleException(String action, Throwable t) {
        logger.warn(t, "Unhandled exception caught during '{}'", action);
    }

}

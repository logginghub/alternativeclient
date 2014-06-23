package com.logginghub.utils;

public class NotImplementedException extends FormattedRuntimeException {
    private static final long serialVersionUID = 1L;

    public NotImplementedException() {
        super();
    }

    public NotImplementedException(String message, Object... objects) {
        super(message, objects);
    }

    public NotImplementedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotImplementedException(Throwable t, String message, Object... objects) {
        super(t, message, objects);
    }

    public NotImplementedException(Throwable cause) {
        super(cause);
    }

}

package com.ecmp.flow.common.util;


public class FlowException extends  RuntimeException {
    public FlowException() {
        super();
    }
    public FlowException(String message) {
        super(message);
    }

    public FlowException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.rivelino.notification.domain.exception;

public class NotificationDeliveryException extends RuntimeException {

    private final boolean retryable;
    private final String errorCode;

    public NotificationDeliveryException(String message, boolean retryable, String errorCode) {
        super(message);
        this.retryable = retryable;
        this.errorCode = errorCode;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

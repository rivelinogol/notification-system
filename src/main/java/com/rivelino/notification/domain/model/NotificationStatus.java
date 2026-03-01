package com.rivelino.notification.domain.model;

public enum NotificationStatus {
    PENDING,
    PROCESSING,
    RETRY_PENDING,
    SENT,
    FAILED,
    DEAD_LETTER,
    SUPPRESSED
}

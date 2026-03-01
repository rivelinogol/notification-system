package com.rivelino.notification.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Notification {

    private final UUID id;
    private final String idempotencyKey;
    private final NotificationType type;
    private final String recipient;
    private final String subject;
    private final String body;
    private final NotificationChannel channel;
    private final Instant createdAt;
    private final int maxAttempts;

    private NotificationStatus status;
    private int attemptCount;
    private String errorMessage;
    private Instant sentAt;

    public Notification(
            UUID id,
            String idempotencyKey,
            NotificationType type,
            String recipient,
            String subject,
            String body,
            NotificationChannel channel,
            Instant createdAt,
            int maxAttempts,
            NotificationStatus status,
            int attemptCount
    ) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.type = type;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.channel = channel;
        this.createdAt = createdAt;
        this.maxAttempts = maxAttempts;
        this.status = status;
        this.attemptCount = attemptCount;
    }

    public UUID getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public NotificationType getType() {
        return type;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void markProcessing() {
        this.status = NotificationStatus.PROCESSING;
        this.errorMessage = null;
        this.attemptCount += 1;
    }

    public void markSent(Instant sentAt) {
        this.status = NotificationStatus.SENT;
        this.errorMessage = null;
        this.sentAt = sentAt;
    }

    public void markRetryPending(String errorMessage) {
        this.status = NotificationStatus.RETRY_PENDING;
        this.errorMessage = errorMessage;
    }

    public void markFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void markDeadLetter() {
        this.status = NotificationStatus.DEAD_LETTER;
    }

    public void markSuppressed(String reason) {
        this.status = NotificationStatus.SUPPRESSED;
        this.errorMessage = reason;
    }

    public boolean canRetry() {
        return attemptCount < maxAttempts;
    }
}

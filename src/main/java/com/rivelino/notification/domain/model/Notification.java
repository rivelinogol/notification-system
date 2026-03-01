package com.rivelino.notification.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Notification {

    private final UUID id;
    private final String idempotencyKey;
    private final String recipient;
    private final String subject;
    private final String body;
    private final NotificationChannel channel;
    private final Instant createdAt;
    private NotificationStatus status;
    private String errorMessage;

    public Notification(
            UUID id,
            String idempotencyKey,
            String recipient,
            String subject,
            String body,
            NotificationChannel channel,
            Instant createdAt,
            NotificationStatus status
    ) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.channel = channel;
        this.createdAt = createdAt;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
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

    public NotificationStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void markProcessing() {
        this.status = NotificationStatus.PROCESSING;
        this.errorMessage = null;
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}

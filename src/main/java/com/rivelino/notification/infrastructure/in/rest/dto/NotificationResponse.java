package com.rivelino.notification.infrastructure.in.rest.dto;

import com.rivelino.notification.domain.model.Notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String idempotencyKey,
        String type,
        String channel,
        String recipient,
        String subject,
        String body,
        String status,
        int attemptCount,
        int maxAttempts,
        String errorMessage,
        Instant createdAt,
        Instant sentAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getIdempotencyKey(),
                notification.getType().name(),
                notification.getChannel().name(),
                notification.getRecipient(),
                notification.getSubject(),
                notification.getBody(),
                notification.getStatus().name(),
                notification.getAttemptCount(),
                notification.getMaxAttempts(),
                notification.getErrorMessage(),
                notification.getCreatedAt(),
                notification.getSentAt()
        );
    }
}

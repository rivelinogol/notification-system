package com.rivelino.notification.application.dto;

import com.rivelino.notification.domain.model.NotificationChannel;

public record SubmitNotificationCommand(
        String idempotencyKey,
        String recipient,
        String subject,
        String body,
        NotificationChannel channel
) {
}

package com.rivelino.notification.application.dto;

import com.rivelino.notification.domain.model.NotificationChannel;
import com.rivelino.notification.domain.model.NotificationType;

public record SubmitNotificationCommand(
        String idempotencyKey,
        String recipient,
        NotificationChannel channel,
        NotificationType type,
        String customSubject,
        String customBody
) {
}

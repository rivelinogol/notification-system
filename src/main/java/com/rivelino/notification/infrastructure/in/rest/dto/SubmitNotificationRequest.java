package com.rivelino.notification.infrastructure.in.rest.dto;

import com.rivelino.notification.domain.model.NotificationChannel;
import com.rivelino.notification.domain.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitNotificationRequest(
        @NotBlank String idempotencyKey,
        @NotBlank String recipient,
        @NotNull NotificationChannel channel,
        @NotNull NotificationType type,
        String customSubject,
        String customBody
) {
}

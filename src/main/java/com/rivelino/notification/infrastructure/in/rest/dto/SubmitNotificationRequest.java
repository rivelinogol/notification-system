package com.rivelino.notification.infrastructure.in.rest.dto;

import com.rivelino.notification.domain.model.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitNotificationRequest(
        @NotBlank String idempotencyKey,
        @NotBlank String recipient,
        @NotBlank String subject,
        @NotBlank String body,
        @NotNull NotificationChannel channel
) {
}

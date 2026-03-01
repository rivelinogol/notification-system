package com.rivelino.notification.application.dto;

import java.util.UUID;

public record SubmitNotificationResult(UUID notificationId, boolean duplicate) {
}

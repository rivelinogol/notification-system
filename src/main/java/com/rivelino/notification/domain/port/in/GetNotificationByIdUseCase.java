package com.rivelino.notification.domain.port.in;

import com.rivelino.notification.domain.model.Notification;

import java.util.Optional;
import java.util.UUID;

public interface GetNotificationByIdUseCase {

    Optional<Notification> getById(UUID notificationId);
}

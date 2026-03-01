package com.rivelino.notification.domain.port.out;

import com.rivelino.notification.domain.model.Notification;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepositoryPort {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    Optional<Notification> findByIdempotencyKey(String idempotencyKey);
}

package com.rivelino.notification.infrastructure.out.repository;

import com.rivelino.notification.domain.model.Notification;
import com.rivelino.notification.domain.port.out.NotificationRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryNotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final Map<UUID, Notification> byId = new ConcurrentHashMap<>();
    private final Map<String, UUID> byIdempotencyKey = new ConcurrentHashMap<>();

    @Override
    public Notification save(Notification notification) {
        byId.put(notification.getId(), notification);
        byIdempotencyKey.put(notification.getIdempotencyKey(), notification.getId());
        return notification;
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<Notification> findByIdempotencyKey(String idempotencyKey) {
        var id = byIdempotencyKey.get(idempotencyKey);
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byId.get(id));
    }
}

package com.rivelino.notification.domain.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface NotificationQueuePort {

    void enqueue(UUID notificationId, Instant availableAt);

    Optional<UUID> dequeueReady(Instant now);
}

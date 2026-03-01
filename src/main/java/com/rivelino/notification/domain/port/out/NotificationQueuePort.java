package com.rivelino.notification.domain.port.out;

import java.util.Optional;
import java.util.UUID;

public interface NotificationQueuePort {

    void enqueue(UUID notificationId);

    Optional<UUID> dequeue();
}

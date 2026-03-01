package com.rivelino.notification.domain.port.out;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyCachePort {

    Optional<UUID> get(String key);

    void put(String key, UUID notificationId);
}

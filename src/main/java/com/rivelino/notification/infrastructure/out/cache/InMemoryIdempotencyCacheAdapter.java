package com.rivelino.notification.infrastructure.out.cache;

import com.rivelino.notification.domain.port.out.IdempotencyCachePort;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryIdempotencyCacheAdapter implements IdempotencyCachePort {

    private final Map<String, UUID> memory = new ConcurrentHashMap<>();

    @Override
    public Optional<UUID> get(String key) {
        return Optional.ofNullable(memory.get(key));
    }

    @Override
    public void put(String key, UUID notificationId) {
        memory.put(key, notificationId);
    }
}

package com.rivelino.notification.infrastructure.out.repository;

import com.rivelino.notification.domain.model.Notification;
import com.rivelino.notification.domain.port.out.DeadLetterStorePort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryDeadLetterStoreAdapter implements DeadLetterStorePort {

    private final CopyOnWriteArrayList<Notification> deadLetters = new CopyOnWriteArrayList<>();

    @Override
    public void store(Notification notification) {
        deadLetters.add(notification);
    }

    @Override
    public List<Notification> findAll() {
        return List.copyOf(deadLetters);
    }
}

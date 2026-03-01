package com.rivelino.notification.infrastructure.out.queue;

import com.rivelino.notification.domain.port.out.NotificationQueuePort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class InMemoryNotificationQueueAdapter implements NotificationQueuePort {

    private final ConcurrentLinkedQueue<UUID> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void enqueue(UUID notificationId) {
        queue.offer(notificationId);
    }

    @Override
    public Optional<UUID> dequeue() {
        return Optional.ofNullable(queue.poll());
    }
}

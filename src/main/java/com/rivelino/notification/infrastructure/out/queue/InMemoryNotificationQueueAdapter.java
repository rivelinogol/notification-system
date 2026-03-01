package com.rivelino.notification.infrastructure.out.queue;

import com.rivelino.notification.domain.port.out.NotificationQueuePort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryNotificationQueueAdapter implements NotificationQueuePort {

    private final PriorityBlockingQueue<QueueItem> queue =
            new PriorityBlockingQueue<>(32, Comparator.comparing(QueueItem::availableAt).thenComparingLong(QueueItem::sequence));
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public void enqueue(UUID notificationId, Instant availableAt) {
        queue.offer(new QueueItem(notificationId, availableAt, sequence.incrementAndGet()));
    }

    @Override
    public Optional<UUID> dequeueReady(Instant now) {
        QueueItem next = queue.peek();
        if (next == null || next.availableAt().isAfter(now)) {
            return Optional.empty();
        }

        QueueItem polled = queue.poll();
        return Optional.ofNullable(polled).map(QueueItem::notificationId);
    }

    private record QueueItem(UUID notificationId, Instant availableAt, long sequence) {
    }
}

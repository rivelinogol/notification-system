package com.rivelino.notification.application;

import com.rivelino.notification.application.dto.SubmitNotificationCommand;
import com.rivelino.notification.application.dto.SubmitNotificationResult;
import com.rivelino.notification.domain.model.Notification;
import com.rivelino.notification.domain.model.NotificationStatus;
import com.rivelino.notification.domain.port.in.ProcessNotificationQueueUseCase;
import com.rivelino.notification.domain.port.in.SubmitNotificationUseCase;
import com.rivelino.notification.domain.port.out.IdempotencyCachePort;
import com.rivelino.notification.domain.port.out.NotificationDeliveryPort;
import com.rivelino.notification.domain.port.out.NotificationQueuePort;
import com.rivelino.notification.domain.port.out.NotificationRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationApplicationService implements SubmitNotificationUseCase, ProcessNotificationQueueUseCase {

    private final NotificationRepositoryPort notificationRepository;
    private final NotificationQueuePort queue;
    private final IdempotencyCachePort idempotencyCache;
    private final NotificationDeliveryPort delivery;

    public NotificationApplicationService(
            NotificationRepositoryPort notificationRepository,
            NotificationQueuePort queue,
            IdempotencyCachePort idempotencyCache,
            NotificationDeliveryPort delivery
    ) {
        this.notificationRepository = notificationRepository;
        this.queue = queue;
        this.idempotencyCache = idempotencyCache;
        this.delivery = delivery;
    }

    @Override
    public SubmitNotificationResult submit(SubmitNotificationCommand command) {
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException("idempotencyKey is required");
        }

        var existingFromCache = idempotencyCache.get(command.idempotencyKey());
        if (existingFromCache.isPresent()) {
            return new SubmitNotificationResult(existingFromCache.get(), true);
        }

        var existingFromRepository = notificationRepository.findByIdempotencyKey(command.idempotencyKey());
        if (existingFromRepository.isPresent()) {
            idempotencyCache.put(command.idempotencyKey(), existingFromRepository.get().getId());
            return new SubmitNotificationResult(existingFromRepository.get().getId(), true);
        }

        var notification = new Notification(
                UUID.randomUUID(),
                command.idempotencyKey(),
                command.recipient(),
                command.subject(),
                command.body(),
                command.channel(),
                Instant.now(),
                NotificationStatus.PENDING
        );

        var saved = notificationRepository.save(notification);
        idempotencyCache.put(saved.getIdempotencyKey(), saved.getId());
        queue.enqueue(saved.getId());

        return new SubmitNotificationResult(saved.getId(), false);
    }

    @Override
    public void processNext() {
        var next = queue.dequeue();
        if (next.isEmpty()) {
            return;
        }

        var notificationId = next.get();
        var notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalStateException("Notification not found: " + notificationId));

        try {
            notification.markProcessing();
            notificationRepository.save(notification);

            delivery.send(notification);

            notification.markSent();
            notificationRepository.save(notification);
        } catch (Exception ex) {
            notification.markFailed(ex.getMessage());
            notificationRepository.save(notification);
        }
    }
}

package com.rivelino.notification.application;

import com.rivelino.notification.application.dto.SubmitNotificationCommand;
import com.rivelino.notification.application.dto.SubmitNotificationResult;
import com.rivelino.notification.domain.exception.NotificationDeliveryException;
import com.rivelino.notification.domain.model.Notification;
import com.rivelino.notification.domain.model.NotificationStatsSnapshot;
import com.rivelino.notification.domain.model.NotificationStatus;
import com.rivelino.notification.domain.port.in.GetDeadLettersUseCase;
import com.rivelino.notification.domain.port.in.GetNotificationByIdUseCase;
import com.rivelino.notification.domain.port.in.GetNotificationStatsUseCase;
import com.rivelino.notification.domain.port.in.ProcessNotificationQueueUseCase;
import com.rivelino.notification.domain.port.in.SubmitNotificationUseCase;
import com.rivelino.notification.domain.port.out.ClockPort;
import com.rivelino.notification.domain.port.out.DeadLetterStorePort;
import com.rivelino.notification.domain.port.out.IdempotencyCachePort;
import com.rivelino.notification.domain.port.out.MessageTemplatePort;
import com.rivelino.notification.domain.port.out.NotificationDeliveryPort;
import com.rivelino.notification.domain.port.out.NotificationMetricsPort;
import com.rivelino.notification.domain.port.out.NotificationQueuePort;
import com.rivelino.notification.domain.port.out.NotificationRepositoryPort;
import com.rivelino.notification.domain.port.out.QuietHoursPolicyPort;
import com.rivelino.notification.domain.port.out.RecipientPreferencePort;
import com.rivelino.notification.domain.port.out.RetryBackoffPolicyPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationApplicationService implements
        SubmitNotificationUseCase,
        ProcessNotificationQueueUseCase,
        GetNotificationByIdUseCase,
        GetDeadLettersUseCase,
        GetNotificationStatsUseCase {

    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    private final NotificationRepositoryPort notificationRepository;
    private final NotificationQueuePort queue;
    private final IdempotencyCachePort idempotencyCache;
    private final NotificationDeliveryPort delivery;
    private final MessageTemplatePort templatePort;
    private final DeadLetterStorePort deadLetterStore;
    private final RetryBackoffPolicyPort retryBackoffPolicy;
    private final RecipientPreferencePort recipientPreference;
    private final QuietHoursPolicyPort quietHoursPolicy;
    private final NotificationMetricsPort metrics;
    private final ClockPort clock;

    public NotificationApplicationService(
            NotificationRepositoryPort notificationRepository,
            NotificationQueuePort queue,
            IdempotencyCachePort idempotencyCache,
            NotificationDeliveryPort delivery,
            MessageTemplatePort templatePort,
            DeadLetterStorePort deadLetterStore,
            RetryBackoffPolicyPort retryBackoffPolicy,
            RecipientPreferencePort recipientPreference,
            QuietHoursPolicyPort quietHoursPolicy,
            NotificationMetricsPort metrics,
            ClockPort clock
    ) {
        this.notificationRepository = notificationRepository;
        this.queue = queue;
        this.idempotencyCache = idempotencyCache;
        this.delivery = delivery;
        this.templatePort = templatePort;
        this.deadLetterStore = deadLetterStore;
        this.retryBackoffPolicy = retryBackoffPolicy;
        this.recipientPreference = recipientPreference;
        this.quietHoursPolicy = quietHoursPolicy;
        this.metrics = metrics;
        this.clock = clock;
    }

    @Override
    public SubmitNotificationResult submit(SubmitNotificationCommand command) {
        validate(command);

        var existingFromCache = idempotencyCache.get(command.idempotencyKey());
        if (existingFromCache.isPresent()) {
            metrics.incrementDuplicate();
            return new SubmitNotificationResult(existingFromCache.get(), true);
        }

        var existingFromRepository = notificationRepository.findByIdempotencyKey(command.idempotencyKey());
        if (existingFromRepository.isPresent()) {
            idempotencyCache.put(command.idempotencyKey(), existingFromRepository.get().getId());
            metrics.incrementDuplicate();
            return new SubmitNotificationResult(existingFromRepository.get().getId(), true);
        }

        metrics.incrementSubmitted();

        var template = templatePort.render(command.type(), command.recipient());
        var subject = isBlank(command.customSubject()) ? template.subject() : command.customSubject();
        var body = isBlank(command.customBody()) ? template.body() : command.customBody();
        var now = clock.now();

        var notification = new Notification(
                UUID.randomUUID(),
                command.idempotencyKey(),
                command.type(),
                command.recipient(),
                subject,
                body,
                command.channel(),
                now,
                DEFAULT_MAX_ATTEMPTS,
                NotificationStatus.PENDING,
                0
        );

        if (!recipientPreference.isChannelEnabled(command.recipient(), command.channel())) {
            notification.markSuppressed("Suppressed: channel opt-out");
            var savedSuppressed = notificationRepository.save(notification);
            idempotencyCache.put(savedSuppressed.getIdempotencyKey(), savedSuppressed.getId());
            metrics.incrementSuppressed();
            return new SubmitNotificationResult(savedSuppressed.getId(), false);
        }

        if (!recipientPreference.isTypeEnabled(command.recipient(), command.type())) {
            notification.markSuppressed("Suppressed: type opt-out");
            var savedSuppressed = notificationRepository.save(notification);
            idempotencyCache.put(savedSuppressed.getIdempotencyKey(), savedSuppressed.getId());
            metrics.incrementSuppressed();
            return new SubmitNotificationResult(savedSuppressed.getId(), false);
        }

        Instant availableAt = quietHoursPolicy.deferUntil(command.recipient(), now).orElse(now);

        var saved = notificationRepository.save(notification);
        idempotencyCache.put(saved.getIdempotencyKey(), saved.getId());
        queue.enqueue(saved.getId(), availableAt);

        return new SubmitNotificationResult(saved.getId(), false);
    }

    @Override
    public void processNext() {
        var next = queue.dequeueReady(clock.now());
        if (next.isEmpty()) {
            return;
        }
        processOne(next.get());
    }

    @Override
    public void processBatch(int maxItems) {
        if (maxItems < 1) {
            throw new IllegalArgumentException("maxItems must be >= 1");
        }

        for (int i = 0; i < maxItems; i++) {
            var next = queue.dequeueReady(clock.now());
            if (next.isEmpty()) {
                return;
            }
            processOne(next.get());
        }
    }

    @Override
    public Optional<Notification> getById(UUID notificationId) {
        return notificationRepository.findById(notificationId);
    }

    @Override
    public List<Notification> getDeadLetters() {
        return deadLetterStore.findAll();
    }

    @Override
    public NotificationStatsSnapshot getStats() {
        return metrics.snapshot();
    }

    private void processOne(UUID notificationId) {
        var notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalStateException("Notification not found: " + notificationId));

        try {
            notification.markProcessing();
            notificationRepository.save(notification);

            delivery.send(notification);

            var now = clock.now();
            notification.markSent(now);
            notificationRepository.save(notification);

            metrics.incrementSent();
            metrics.recordSentLatencyMs(now.toEpochMilli() - notification.getCreatedAt().toEpochMilli());
        } catch (NotificationDeliveryException ex) {
            handleFailure(notification, ex.getMessage(), ex.isRetryable());
        } catch (Exception ex) {
            handleFailure(notification, ex.getMessage(), true);
        }
    }

    private void handleFailure(Notification notification, String errorMessage, boolean retryable) {
        if (retryable && notification.canRetry()) {
            notification.markRetryPending(errorMessage);
            notificationRepository.save(notification);

            var delay = retryBackoffPolicy.nextDelayForAttempt(notification.getAttemptCount());
            var nextRetryAt = clock.now().plus(delay);
            queue.enqueue(notification.getId(), nextRetryAt);
            metrics.incrementRetryScheduled();
            return;
        }

        notification.markFailed(errorMessage);
        notification.markDeadLetter();
        notificationRepository.save(notification);
        deadLetterStore.store(notification);
        metrics.incrementFailed();
        metrics.incrementDeadLetter();
    }

    private static void validate(SubmitNotificationCommand command) {
        if (isBlank(command.idempotencyKey())) {
            throw new IllegalArgumentException("idempotencyKey is required");
        }
        if (isBlank(command.recipient())) {
            throw new IllegalArgumentException("recipient is required");
        }
        if (command.channel() == null) {
            throw new IllegalArgumentException("channel is required");
        }
        if (command.type() == null) {
            throw new IllegalArgumentException("type is required");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

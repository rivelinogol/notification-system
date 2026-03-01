package com.rivelino.notification.application;

import com.rivelino.notification.application.dto.SubmitNotificationCommand;
import com.rivelino.notification.domain.model.Notification;
import com.rivelino.notification.domain.model.NotificationChannel;
import com.rivelino.notification.domain.model.NotificationStatus;
import com.rivelino.notification.domain.model.NotificationType;
import com.rivelino.notification.domain.port.out.ClockPort;
import com.rivelino.notification.infrastructure.out.backoff.ExponentialRetryBackoffPolicyAdapter;
import com.rivelino.notification.infrastructure.out.cache.InMemoryIdempotencyCacheAdapter;
import com.rivelino.notification.infrastructure.out.metrics.InMemoryNotificationMetricsAdapter;
import com.rivelino.notification.infrastructure.out.preferences.InMemoryQuietHoursPolicyAdapter;
import com.rivelino.notification.infrastructure.out.preferences.InMemoryRecipientPreferenceAdapter;
import com.rivelino.notification.infrastructure.out.provider.EmailChannelSenderAdapter;
import com.rivelino.notification.infrastructure.out.provider.PushChannelSenderAdapter;
import com.rivelino.notification.infrastructure.out.provider.RoutingNotificationDeliveryAdapter;
import com.rivelino.notification.infrastructure.out.provider.SmsChannelSenderAdapter;
import com.rivelino.notification.infrastructure.out.queue.InMemoryNotificationQueueAdapter;
import com.rivelino.notification.infrastructure.out.ratelimit.InMemoryRateLimitAdapter;
import com.rivelino.notification.infrastructure.out.repository.InMemoryDeadLetterStoreAdapter;
import com.rivelino.notification.infrastructure.out.repository.InMemoryNotificationRepositoryAdapter;
import com.rivelino.notification.infrastructure.out.template.StubMessageTemplateAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationApplicationServiceTest {

    private NotificationApplicationService service;
    private TestClock clock;

    @BeforeEach
    void setUp() {
        clock = new TestClock(Instant.parse("2026-01-01T12:00:00Z"));

        service = new NotificationApplicationService(
                new InMemoryNotificationRepositoryAdapter(),
                new InMemoryNotificationQueueAdapter(),
                new InMemoryIdempotencyCacheAdapter(),
                new RoutingNotificationDeliveryAdapter(List.of(
                        new EmailChannelSenderAdapter(),
                        new SmsChannelSenderAdapter(),
                        new PushChannelSenderAdapter()
                )),
                new StubMessageTemplateAdapter(),
                new InMemoryDeadLetterStoreAdapter(),
                new ExponentialRetryBackoffPolicyAdapter(),
                new InMemoryRecipientPreferenceAdapter(),
                new InMemoryQuietHoursPolicyAdapter(),
                new InMemoryRateLimitAdapter(),
                new InMemoryNotificationMetricsAdapter(),
                clock
        );
    }

    @Test
    void duplicateIdempotencyKeyShouldNotCreateSecondNotification() {
        var first = service.submit(command("idem-1", "alice@example.com"));
        var second = service.submit(command("idem-1", "alice@example.com"));

        assertFalse(first.duplicate());
        assertTrue(second.duplicate());
        assertEquals(first.notificationId(), second.notificationId());

        var stats = service.getStats();
        assertEquals(1, stats.submittedTotal());
        assertEquals(1, stats.duplicateTotal());
    }

    @Test
    void nonRetryableErrorShouldGoDirectlyToDeadLetter() {
        var result = service.submit(command("idem-2", "user-invalid-email@example.com"));
        service.processBatch(10);

        Notification notification = requireNotification(result.notificationId());
        assertEquals(NotificationStatus.DEAD_LETTER, notification.getStatus());
        assertEquals(1, notification.getAttemptCount());

        var stats = service.getStats();
        assertEquals(0, stats.retryScheduledTotal());
        assertEquals(1, stats.failedTotal());
        assertEquals(1, stats.deadLetterTotal());
    }

    @Test
    void retryableErrorShouldRetryWithBackoffUntilDeadLetter() {
        var result = service.submit(command("idem-3", "user-fail-email@example.com"));

        service.processNext();
        Notification firstAttempt = requireNotification(result.notificationId());
        assertEquals(NotificationStatus.RETRY_PENDING, firstAttempt.getStatus());
        assertEquals(1, firstAttempt.getAttemptCount());

        service.processNext();
        Notification stillWaiting = requireNotification(result.notificationId());
        assertEquals(NotificationStatus.RETRY_PENDING, stillWaiting.getStatus());
        assertEquals(1, stillWaiting.getAttemptCount());

        clock.advanceSeconds(5);
        service.processNext();
        Notification secondAttempt = requireNotification(result.notificationId());
        assertEquals(NotificationStatus.RETRY_PENDING, secondAttempt.getStatus());
        assertEquals(2, secondAttempt.getAttemptCount());

        clock.advanceSeconds(10);
        service.processNext();
        Notification finalAttempt = requireNotification(result.notificationId());
        assertEquals(NotificationStatus.DEAD_LETTER, finalAttempt.getStatus());
        assertEquals(3, finalAttempt.getAttemptCount());

        var stats = service.getStats();
        assertEquals(2, stats.retryScheduledTotal());
        assertEquals(1, stats.failedTotal());
        assertEquals(1, stats.deadLetterTotal());
    }

    @Test
    void quietHoursShouldDeferDelivery() {
        clock.setNow(Instant.parse("2026-01-01T23:00:00Z"));

        var result = service.submit(command("idem-4", "alice-quiet-night@example.com"));

        service.processNext();
        Notification beforeAllowedWindow = requireNotification(result.notificationId());
        assertEquals(NotificationStatus.PENDING, beforeAllowedWindow.getStatus());
        assertNull(beforeAllowedWindow.getSentAt());

        clock.advanceSeconds(9 * 3600L);
        service.processNext();

        Notification afterAllowedWindow = requireNotification(result.notificationId());
        assertEquals(NotificationStatus.SENT, afterAllowedWindow.getStatus());
        assertNotNull(afterAllowedWindow.getSentAt());
    }

    @Test
    void optOutShouldSuppressNotification() {
        var result = service.submit(command("idem-5", "user-optout-email@example.com"));

        Notification notification = requireNotification(result.notificationId());
        assertEquals(NotificationStatus.SUPPRESSED, notification.getStatus());

        var stats = service.getStats();
        assertEquals(1, stats.suppressedTotal());
        assertEquals(0, stats.rateLimitedTotal());
    }

    @Test
    void rateLimitShouldSuppressAfterFifthMessageInSameMinute() {
        UUID lastId = null;
        for (int i = 1; i <= 6; i++) {
            var result = service.submit(command("idem-rate-" + i, "burst@example.com"));
            lastId = result.notificationId();
        }

        Notification sixth = requireNotification(lastId);
        assertEquals(NotificationStatus.SUPPRESSED, sixth.getStatus());
        assertTrue(sixth.getErrorMessage().contains("rate limit"));

        var stats = service.getStats();
        assertEquals(6, stats.submittedTotal());
        assertEquals(1, stats.suppressedTotal());
        assertEquals(1, stats.rateLimitedTotal());
    }

    private SubmitNotificationCommand command(String idempotencyKey, String recipient) {
        return new SubmitNotificationCommand(
                idempotencyKey,
                recipient,
                NotificationChannel.EMAIL,
                NotificationType.BOOKING_CONFIRMED,
                null,
                null
        );
    }

    private Notification requireNotification(UUID id) {
        return service.getById(id)
                .orElseThrow(() -> new AssertionError("Notification not found: " + id));
    }

    private static final class TestClock implements ClockPort {
        private Instant now;

        private TestClock(Instant now) {
            this.now = now;
        }

        @Override
        public Instant now() {
            return now;
        }

        private void setNow(Instant now) {
            this.now = now;
        }

        private void advanceSeconds(long seconds) {
            this.now = this.now.plusSeconds(seconds);
        }
    }
}

package com.rivelino.notification.infrastructure.out.metrics;

import com.rivelino.notification.domain.model.NotificationStatsSnapshot;
import com.rivelino.notification.domain.port.out.NotificationMetricsPort;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.LongAdder;

@Component
public class InMemoryNotificationMetricsAdapter implements NotificationMetricsPort {

    private final LongAdder submittedTotal = new LongAdder();
    private final LongAdder duplicateTotal = new LongAdder();
    private final LongAdder suppressedTotal = new LongAdder();
    private final LongAdder rateLimitedTotal = new LongAdder();
    private final LongAdder sentTotal = new LongAdder();
    private final LongAdder retryScheduledTotal = new LongAdder();
    private final LongAdder failedTotal = new LongAdder();
    private final LongAdder deadLetterTotal = new LongAdder();
    private final LongAdder sentLatencySamples = new LongAdder();
    private final LongAdder sentLatencyTotalMs = new LongAdder();

    @Override
    public void incrementSubmitted() {
        submittedTotal.increment();
    }

    @Override
    public void incrementDuplicate() {
        duplicateTotal.increment();
    }

    @Override
    public void incrementSuppressed() {
        suppressedTotal.increment();
    }

    @Override
    public void incrementRateLimited() {
        rateLimitedTotal.increment();
    }

    @Override
    public void incrementSent() {
        sentTotal.increment();
    }

    @Override
    public void incrementRetryScheduled() {
        retryScheduledTotal.increment();
    }

    @Override
    public void incrementFailed() {
        failedTotal.increment();
    }

    @Override
    public void incrementDeadLetter() {
        deadLetterTotal.increment();
    }

    @Override
    public void recordSentLatencyMs(long latencyMs) {
        sentLatencySamples.increment();
        sentLatencyTotalMs.add(Math.max(0, latencyMs));
    }

    @Override
    public NotificationStatsSnapshot snapshot() {
        long samples = sentLatencySamples.sum();
        long avgLatency = samples == 0 ? 0 : sentLatencyTotalMs.sum() / samples;

        return new NotificationStatsSnapshot(
                submittedTotal.sum(),
                duplicateTotal.sum(),
                suppressedTotal.sum(),
                rateLimitedTotal.sum(),
                sentTotal.sum(),
                retryScheduledTotal.sum(),
                failedTotal.sum(),
                deadLetterTotal.sum(),
                samples,
                avgLatency
        );
    }
}

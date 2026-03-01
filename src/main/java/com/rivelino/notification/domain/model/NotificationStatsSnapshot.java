package com.rivelino.notification.domain.model;

public record NotificationStatsSnapshot(
        long submittedTotal,
        long duplicateTotal,
        long suppressedTotal,
        long rateLimitedTotal,
        long sentTotal,
        long retryScheduledTotal,
        long failedTotal,
        long deadLetterTotal,
        long sentLatencySamples,
        long averageSentLatencyMs
) {
}

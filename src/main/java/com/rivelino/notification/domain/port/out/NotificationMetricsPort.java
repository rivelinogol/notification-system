package com.rivelino.notification.domain.port.out;

import com.rivelino.notification.domain.model.NotificationStatsSnapshot;

public interface NotificationMetricsPort {

    void incrementSubmitted();

    void incrementDuplicate();

    void incrementSuppressed();

    void incrementSent();

    void incrementRetryScheduled();

    void incrementFailed();

    void incrementDeadLetter();

    void recordSentLatencyMs(long latencyMs);

    NotificationStatsSnapshot snapshot();
}

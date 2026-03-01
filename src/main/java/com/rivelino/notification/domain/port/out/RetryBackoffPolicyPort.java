package com.rivelino.notification.domain.port.out;

import java.time.Duration;

public interface RetryBackoffPolicyPort {

    Duration nextDelayForAttempt(int attemptCount);
}

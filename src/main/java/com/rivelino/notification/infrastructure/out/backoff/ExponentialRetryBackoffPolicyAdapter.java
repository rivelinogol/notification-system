package com.rivelino.notification.infrastructure.out.backoff;

import com.rivelino.notification.domain.port.out.RetryBackoffPolicyPort;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ExponentialRetryBackoffPolicyAdapter implements RetryBackoffPolicyPort {

    private static final int BASE_SECONDS = 5;
    private static final int MAX_SECONDS = 60;

    @Override
    public Duration nextDelayForAttempt(int attemptCount) {
        // attemptCount starts at 1 on first processing try.
        int exponent = Math.max(0, attemptCount - 1);
        long seconds = (long) BASE_SECONDS << exponent;
        if (seconds > MAX_SECONDS) {
            seconds = MAX_SECONDS;
        }
        return Duration.ofSeconds(seconds);
    }
}

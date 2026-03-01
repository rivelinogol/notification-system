package com.rivelino.notification.domain.port.out;

import com.rivelino.notification.domain.model.NotificationChannel;

import java.time.Instant;
import java.util.Optional;

public interface RateLimitPort {

    Optional<Instant> blockedUntil(String recipient, NotificationChannel channel, Instant now);
}

package com.rivelino.notification.infrastructure.out.ratelimit;

import com.rivelino.notification.domain.model.NotificationChannel;
import com.rivelino.notification.domain.port.out.RateLimitPort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class InMemoryRateLimitAdapter implements RateLimitPort {

    private static final int MAX_PER_MINUTE = 5;
    private static final long WINDOW_SECONDS = 60;

    private final ConcurrentHashMap<String, WindowCounter> windows = new ConcurrentHashMap<>();

    @Override
    public Optional<Instant> blockedUntil(String recipient, NotificationChannel channel, Instant now) {
        String key = recipient + "|" + channel.name();
        Instant windowStart = now.truncatedTo(ChronoUnit.MINUTES);

        WindowCounter counter = windows.compute(key, (ignored, current) -> {
            if (current == null || !current.windowStart.equals(windowStart)) {
                return new WindowCounter(windowStart, new AtomicInteger(0));
            }
            return current;
        });

        int current = counter.count.get();
        if (current >= MAX_PER_MINUTE) {
            return Optional.of(counter.windowStart.plusSeconds(WINDOW_SECONDS));
        }

        counter.count.incrementAndGet();
        return Optional.empty();
    }

    private static final class WindowCounter {
        private final Instant windowStart;
        private final AtomicInteger count;

        private WindowCounter(Instant windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}

package com.rivelino.notification.infrastructure.out.clock;

import com.rivelino.notification.domain.port.out.ClockPort;
import com.rivelino.notification.domain.port.out.SimulationClockControlPort;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class InMemoryAdjustableClockAdapter implements ClockPort, SimulationClockControlPort {

    private final AtomicReference<Instant> now = new AtomicReference<>(Instant.now());

    @Override
    public Instant now() {
        return now.get();
    }

    @Override
    public Instant advance(Duration duration) {
        return now.updateAndGet(current -> current.plus(duration));
    }

    @Override
    public Instant resetToSystemNow() {
        Instant systemNow = Instant.now();
        now.set(systemNow);
        return systemNow;
    }
}

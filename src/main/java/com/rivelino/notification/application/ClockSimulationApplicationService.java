package com.rivelino.notification.application;

import com.rivelino.notification.domain.port.in.SimulationClockUseCase;
import com.rivelino.notification.domain.port.out.ClockPort;
import com.rivelino.notification.domain.port.out.SimulationClockControlPort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class ClockSimulationApplicationService implements SimulationClockUseCase {

    private final ClockPort clock;
    private final SimulationClockControlPort clockControl;

    public ClockSimulationApplicationService(ClockPort clock, SimulationClockControlPort clockControl) {
        this.clock = clock;
        this.clockControl = clockControl;
    }

    @Override
    public Instant currentTime() {
        return clock.now();
    }

    @Override
    public Instant advanceSeconds(long seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("seconds must be >= 0");
        }
        return clockControl.advance(Duration.ofSeconds(seconds));
    }

    @Override
    public Instant reset() {
        return clockControl.resetToSystemNow();
    }
}

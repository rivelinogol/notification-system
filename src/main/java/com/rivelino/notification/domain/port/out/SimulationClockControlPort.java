package com.rivelino.notification.domain.port.out;

import java.time.Duration;
import java.time.Instant;

public interface SimulationClockControlPort {

    Instant advance(Duration duration);

    Instant resetToSystemNow();
}

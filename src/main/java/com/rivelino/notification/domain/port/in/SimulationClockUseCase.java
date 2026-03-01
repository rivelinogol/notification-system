package com.rivelino.notification.domain.port.in;

import java.time.Instant;

public interface SimulationClockUseCase {

    Instant currentTime();

    Instant advanceSeconds(long seconds);

    Instant reset();
}

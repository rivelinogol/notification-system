package com.rivelino.notification.domain.port.out;

import java.time.Instant;

public interface ClockPort {

    Instant now();
}

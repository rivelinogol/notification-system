package com.rivelino.notification.domain.port.out;

import java.time.Instant;
import java.util.Optional;

public interface QuietHoursPolicyPort {

    Optional<Instant> deferUntil(String recipient, Instant now);
}

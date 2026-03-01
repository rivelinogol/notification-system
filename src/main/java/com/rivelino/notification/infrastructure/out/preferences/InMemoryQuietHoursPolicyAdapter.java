package com.rivelino.notification.infrastructure.out.preferences;

import com.rivelino.notification.domain.port.out.QuietHoursPolicyPort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Component
public class InMemoryQuietHoursPolicyAdapter implements QuietHoursPolicyPort {

    private static final LocalTime START = LocalTime.of(22, 0);
    private static final LocalTime END = LocalTime.of(8, 0);

    @Override
    public Optional<Instant> deferUntil(String recipient, Instant now) {
        if (!recipient.contains("quiet-night")) {
            return Optional.empty();
        }

        var dateTime = LocalDateTime.ofInstant(now, ZoneOffset.UTC);
        var localTime = dateTime.toLocalTime();
        if (!isInQuietHours(localTime)) {
            return Optional.empty();
        }

        LocalDate nextDate = localTime.isBefore(END) ? dateTime.toLocalDate() : dateTime.toLocalDate().plusDays(1);
        Instant nextAllowed = LocalDateTime.of(nextDate, END).toInstant(ZoneOffset.UTC);
        return Optional.of(nextAllowed);
    }

    private static boolean isInQuietHours(LocalTime time) {
        return !time.isBefore(START) || time.isBefore(END);
    }
}

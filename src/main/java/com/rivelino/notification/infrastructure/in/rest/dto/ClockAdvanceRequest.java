package com.rivelino.notification.infrastructure.in.rest.dto;

import jakarta.validation.constraints.Min;

public record ClockAdvanceRequest(@Min(0) long seconds) {
}

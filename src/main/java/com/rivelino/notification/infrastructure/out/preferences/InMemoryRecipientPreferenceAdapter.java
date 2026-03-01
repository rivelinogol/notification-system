package com.rivelino.notification.infrastructure.out.preferences;

import com.rivelino.notification.domain.model.NotificationChannel;
import com.rivelino.notification.domain.model.NotificationType;
import com.rivelino.notification.domain.port.out.RecipientPreferencePort;
import org.springframework.stereotype.Component;

@Component
public class InMemoryRecipientPreferenceAdapter implements RecipientPreferencePort {

    @Override
    public boolean isChannelEnabled(String recipient, NotificationChannel channel) {
        if (recipient.contains("optout-all")) {
            return false;
        }

        return switch (channel) {
            case EMAIL -> !recipient.contains("optout-email");
            case SMS -> !recipient.contains("optout-sms");
            case PUSH -> !recipient.contains("optout-push");
        };
    }

    @Override
    public boolean isTypeEnabled(String recipient, NotificationType type) {
        return switch (type) {
            case BOOKING_CONFIRMED -> !recipient.contains("optout-booking-confirmed");
            case BOOKING_CANCELLED -> !recipient.contains("optout-booking-cancelled");
            case PAYMENT_FAILED -> !recipient.contains("optout-payment-failed");
            case EVENT_REMINDER -> !recipient.contains("optout-event-reminder");
        };
    }
}

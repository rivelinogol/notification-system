package com.rivelino.notification.infrastructure.out.template;

import com.rivelino.notification.domain.model.NotificationType;
import com.rivelino.notification.domain.port.out.MessageTemplatePort;
import org.springframework.stereotype.Component;

@Component
public class StubMessageTemplateAdapter implements MessageTemplatePort {

    @Override
    public TemplateMessage render(NotificationType type, String recipient) {
        return switch (type) {
            case BOOKING_CONFIRMED -> new TemplateMessage(
                    "Booking confirmed",
                    "Hello " + recipient + ", your booking is confirmed."
            );
            case BOOKING_CANCELLED -> new TemplateMessage(
                    "Booking cancelled",
                    "Hello " + recipient + ", your booking was cancelled."
            );
            case PAYMENT_FAILED -> new TemplateMessage(
                    "Payment failed",
                    "Hello " + recipient + ", your payment could not be processed."
            );
            case EVENT_REMINDER -> new TemplateMessage(
                    "Event reminder",
                    "Hello " + recipient + ", your event starts soon."
            );
        };
    }
}

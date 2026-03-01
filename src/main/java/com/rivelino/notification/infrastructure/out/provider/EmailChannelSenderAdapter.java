package com.rivelino.notification.infrastructure.out.provider;

import com.rivelino.notification.domain.exception.NotificationDeliveryException;
import com.rivelino.notification.domain.model.Notification;
import com.rivelino.notification.domain.model.NotificationChannel;
import com.rivelino.notification.domain.port.out.ChannelSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailChannelSenderAdapter implements ChannelSenderPort {

    private static final Logger log = LoggerFactory.getLogger(EmailChannelSenderAdapter.class);

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(Notification notification) {
        if (notification.getRecipient().contains("invalid-email")) {
            throw new NotificationDeliveryException(
                    "Simulated email validation error",
                    false,
                    "EMAIL_INVALID_RECIPIENT"
            );
        }

        if (notification.getRecipient().contains("fail-email")) {
            throw new NotificationDeliveryException(
                    "Simulated email provider outage",
                    true,
                    "EMAIL_PROVIDER_UNAVAILABLE"
            );
        }

        log.info("STUB_EMAIL to={} subject={} body={}",
                notification.getRecipient(),
                notification.getSubject(),
                notification.getBody());
    }
}

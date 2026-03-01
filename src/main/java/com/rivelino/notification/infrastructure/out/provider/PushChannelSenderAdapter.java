package com.rivelino.notification.infrastructure.out.provider;

import com.rivelino.notification.domain.exception.NotificationDeliveryException;
import com.rivelino.notification.domain.model.Notification;
import com.rivelino.notification.domain.model.NotificationChannel;
import com.rivelino.notification.domain.port.out.ChannelSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PushChannelSenderAdapter implements ChannelSenderPort {

    private static final Logger log = LoggerFactory.getLogger(PushChannelSenderAdapter.class);

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public void send(Notification notification) {
        if (notification.getRecipient().contains("invalid-push")) {
            throw new NotificationDeliveryException(
                    "Simulated push token validation error",
                    false,
                    "PUSH_INVALID_TOKEN"
            );
        }

        if (notification.getRecipient().contains("fail-push")) {
            throw new NotificationDeliveryException(
                    "Simulated push provider outage",
                    true,
                    "PUSH_PROVIDER_UNAVAILABLE"
            );
        }

        log.info("STUB_PUSH to={} title={} body={}",
                notification.getRecipient(),
                notification.getSubject(),
                notification.getBody());
    }
}

package com.rivelino.notification.infrastructure.out.provider;

import com.rivelino.notification.domain.model.Notification;
import com.rivelino.notification.domain.port.out.NotificationDeliveryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StubNotificationDeliveryAdapter implements NotificationDeliveryPort {

    private static final Logger log = LoggerFactory.getLogger(StubNotificationDeliveryAdapter.class);

    @Override
    public void send(Notification notification) {
        // Stub intencional: no integra con SES/Twilio/Firebase.
        // Solo deja trazabilidad para entender el flujo.
        log.info("STUB_SEND channel={} recipient={} subject={} body={}",
                notification.getChannel(),
                notification.getRecipient(),
                notification.getSubject(),
                notification.getBody());
    }
}

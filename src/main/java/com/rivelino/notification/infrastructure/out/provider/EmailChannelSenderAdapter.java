package com.rivelino.notification.infrastructure.out.provider;

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
        if (notification.getRecipient().contains("fail-email")) {
            throw new IllegalStateException("Simulated email provider failure");
        }
        log.info("STUB_EMAIL to={} subject={} body={}",
                notification.getRecipient(),
                notification.getSubject(),
                notification.getBody());
    }
}

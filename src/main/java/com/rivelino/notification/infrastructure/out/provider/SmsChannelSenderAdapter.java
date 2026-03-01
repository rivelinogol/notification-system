package com.rivelino.notification.infrastructure.out.provider;

import com.rivelino.notification.domain.model.Notification;
import com.rivelino.notification.domain.model.NotificationChannel;
import com.rivelino.notification.domain.port.out.ChannelSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SmsChannelSenderAdapter implements ChannelSenderPort {

    private static final Logger log = LoggerFactory.getLogger(SmsChannelSenderAdapter.class);

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS;
    }

    @Override
    public void send(Notification notification) {
        if (notification.getRecipient().contains("fail-sms")) {
            throw new IllegalStateException("Simulated sms provider failure");
        }
        log.info("STUB_SMS to={} message={}",
                notification.getRecipient(),
                notification.getBody());
    }
}

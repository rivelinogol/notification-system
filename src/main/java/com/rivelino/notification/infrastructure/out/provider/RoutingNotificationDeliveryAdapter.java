package com.rivelino.notification.infrastructure.out.provider;

import com.rivelino.notification.domain.model.Notification;
import com.rivelino.notification.domain.port.out.ChannelSenderPort;
import com.rivelino.notification.domain.port.out.NotificationDeliveryPort;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class RoutingNotificationDeliveryAdapter implements NotificationDeliveryPort {

    private final Map<com.rivelino.notification.domain.model.NotificationChannel, ChannelSenderPort> routing =
            new EnumMap<>(com.rivelino.notification.domain.model.NotificationChannel.class);

    public RoutingNotificationDeliveryAdapter(List<ChannelSenderPort> senders) {
        for (ChannelSenderPort sender : senders) {
            routing.put(sender.channel(), sender);
        }
    }

    @Override
    public void send(Notification notification) {
        var sender = routing.get(notification.getChannel());
        if (sender == null) {
            throw new IllegalStateException("No sender configured for channel " + notification.getChannel());
        }
        sender.send(notification);
    }
}

package com.rivelino.notification.domain.port.out;

import com.rivelino.notification.domain.model.Notification;
import com.rivelino.notification.domain.model.NotificationChannel;

public interface ChannelSenderPort {

    NotificationChannel channel();

    void send(Notification notification);
}

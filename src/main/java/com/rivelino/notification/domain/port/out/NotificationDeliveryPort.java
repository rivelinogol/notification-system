package com.rivelino.notification.domain.port.out;

import com.rivelino.notification.domain.model.Notification;

public interface NotificationDeliveryPort {

    void send(Notification notification);
}

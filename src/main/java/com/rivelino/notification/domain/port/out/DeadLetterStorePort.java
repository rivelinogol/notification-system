package com.rivelino.notification.domain.port.out;

import com.rivelino.notification.domain.model.Notification;

import java.util.List;

public interface DeadLetterStorePort {

    void store(Notification notification);

    List<Notification> findAll();
}

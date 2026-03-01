package com.rivelino.notification.domain.port.in;

import com.rivelino.notification.domain.model.Notification;

import java.util.List;

public interface GetDeadLettersUseCase {

    List<Notification> getDeadLetters();
}

package com.rivelino.notification.domain.port.out;

import com.rivelino.notification.domain.model.NotificationChannel;
import com.rivelino.notification.domain.model.NotificationType;

public interface RecipientPreferencePort {

    boolean isChannelEnabled(String recipient, NotificationChannel channel);

    boolean isTypeEnabled(String recipient, NotificationType type);
}

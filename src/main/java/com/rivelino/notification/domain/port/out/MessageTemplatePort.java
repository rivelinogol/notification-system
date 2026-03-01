package com.rivelino.notification.domain.port.out;

import com.rivelino.notification.domain.model.NotificationType;

public interface MessageTemplatePort {

    TemplateMessage render(NotificationType type, String recipient);

    record TemplateMessage(String subject, String body) {
    }
}

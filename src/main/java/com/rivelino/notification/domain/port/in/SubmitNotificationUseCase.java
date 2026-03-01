package com.rivelino.notification.domain.port.in;

import com.rivelino.notification.application.dto.SubmitNotificationCommand;
import com.rivelino.notification.application.dto.SubmitNotificationResult;

public interface SubmitNotificationUseCase {

    SubmitNotificationResult submit(SubmitNotificationCommand command);
}

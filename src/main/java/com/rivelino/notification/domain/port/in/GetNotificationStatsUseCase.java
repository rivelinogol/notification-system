package com.rivelino.notification.domain.port.in;

import com.rivelino.notification.domain.model.NotificationStatsSnapshot;

public interface GetNotificationStatsUseCase {

    NotificationStatsSnapshot getStats();
}

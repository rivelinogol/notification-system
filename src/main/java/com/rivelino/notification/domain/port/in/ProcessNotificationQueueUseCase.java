package com.rivelino.notification.domain.port.in;

public interface ProcessNotificationQueueUseCase {

    void processNext();

    void processBatch(int maxItems);
}

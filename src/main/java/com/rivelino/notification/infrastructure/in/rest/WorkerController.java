package com.rivelino.notification.infrastructure.in.rest;

import com.rivelino.notification.domain.port.in.ProcessNotificationQueueUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/workers")
public class WorkerController {

    private final ProcessNotificationQueueUseCase processNotificationQueueUseCase;

    public WorkerController(ProcessNotificationQueueUseCase processNotificationQueueUseCase) {
        this.processNotificationQueueUseCase = processNotificationQueueUseCase;
    }

    @PostMapping("/process-next")
    public ResponseEntity<Map<String, String>> processNext() {
        processNotificationQueueUseCase.processNext();
        return ResponseEntity.ok(Map.of("message", "Processed one queued notification (if any)"));
    }

    @PostMapping("/process-batch")
    public ResponseEntity<Map<String, Object>> processBatch(@RequestParam(defaultValue = "10") int maxItems) {
        processNotificationQueueUseCase.processBatch(maxItems);
        return ResponseEntity.ok(Map.of(
                "message", "Processed queued notifications",
                "maxItems", maxItems
        ));
    }

    @Scheduled(fixedDelay = 1000)
    void scheduledProcess() {
        processNotificationQueueUseCase.processBatch(5);
    }
}

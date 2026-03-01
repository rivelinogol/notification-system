package com.rivelino.notification.infrastructure.in.rest;

import com.rivelino.notification.application.dto.SubmitNotificationCommand;
import com.rivelino.notification.domain.port.in.SubmitNotificationUseCase;
import com.rivelino.notification.infrastructure.in.rest.dto.SubmitNotificationRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final SubmitNotificationUseCase submitNotificationUseCase;

    public NotificationController(SubmitNotificationUseCase submitNotificationUseCase) {
        this.submitNotificationUseCase = submitNotificationUseCase;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> submit(@Valid @RequestBody SubmitNotificationRequest request) {
        var result = submitNotificationUseCase.submit(new SubmitNotificationCommand(
                request.idempotencyKey(),
                request.recipient(),
                request.subject(),
                request.body(),
                request.channel()
        ));

        return ResponseEntity.accepted().body(Map.of(
                "notificationId", result.notificationId(),
                "duplicate", result.duplicate(),
                "message", "Notification accepted and queued"
        ));
    }
}

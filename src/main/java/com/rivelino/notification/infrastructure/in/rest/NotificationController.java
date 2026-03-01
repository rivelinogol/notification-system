package com.rivelino.notification.infrastructure.in.rest;

import com.rivelino.notification.application.dto.SubmitNotificationCommand;
import com.rivelino.notification.domain.port.in.GetDeadLettersUseCase;
import com.rivelino.notification.domain.port.in.GetNotificationByIdUseCase;
import com.rivelino.notification.domain.port.in.SubmitNotificationUseCase;
import com.rivelino.notification.infrastructure.in.rest.dto.NotificationResponse;
import com.rivelino.notification.infrastructure.in.rest.dto.SubmitNotificationRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final SubmitNotificationUseCase submitNotificationUseCase;
    private final GetNotificationByIdUseCase getNotificationByIdUseCase;
    private final GetDeadLettersUseCase getDeadLettersUseCase;

    public NotificationController(
            SubmitNotificationUseCase submitNotificationUseCase,
            GetNotificationByIdUseCase getNotificationByIdUseCase,
            GetDeadLettersUseCase getDeadLettersUseCase
    ) {
        this.submitNotificationUseCase = submitNotificationUseCase;
        this.getNotificationByIdUseCase = getNotificationByIdUseCase;
        this.getDeadLettersUseCase = getDeadLettersUseCase;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> submit(@Valid @RequestBody SubmitNotificationRequest request) {
        var result = submitNotificationUseCase.submit(new SubmitNotificationCommand(
                request.idempotencyKey(),
                request.recipient(),
                request.channel(),
                request.type(),
                request.customSubject(),
                request.customBody()
        ));

        return ResponseEntity.accepted().body(Map.of(
                "notificationId", result.notificationId(),
                "duplicate", result.duplicate(),
                "message", "Notification accepted and queued"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getById(@PathVariable UUID id) {
        return getNotificationByIdUseCase.getById(id)
                .map(NotificationResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/dead-letters")
    public List<NotificationResponse> deadLetters() {
        return getDeadLettersUseCase.getDeadLetters().stream()
                .map(NotificationResponse::from)
                .toList();
    }
}

package com.rivelino.notification.infrastructure.in.rest;

import com.rivelino.notification.domain.port.in.SimulationClockUseCase;
import com.rivelino.notification.infrastructure.in.rest.dto.ClockAdvanceRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/simulation/clock")
public class SimulationClockController {

    private final SimulationClockUseCase simulationClockUseCase;

    public SimulationClockController(SimulationClockUseCase simulationClockUseCase) {
        this.simulationClockUseCase = simulationClockUseCase;
    }

    @GetMapping
    public Map<String, Object> now() {
        return Map.of("currentTime", simulationClockUseCase.currentTime());
    }

    @PostMapping("/advance")
    public ResponseEntity<Map<String, Object>> advance(@Valid @RequestBody ClockAdvanceRequest request) {
        var newTime = simulationClockUseCase.advanceSeconds(request.seconds());
        return ResponseEntity.ok(Map.of("currentTime", newTime, "advancedSeconds", request.seconds()));
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> reset() {
        var newTime = simulationClockUseCase.reset();
        return ResponseEntity.ok(Map.of("currentTime", newTime));
    }
}

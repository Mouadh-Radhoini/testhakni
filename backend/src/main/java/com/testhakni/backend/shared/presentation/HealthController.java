package com.testhakni.backend.shared.presentation;

import java.time.Instant;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Verifier que l'API repond")
public class HealthController {

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping("/health")
    @Operation(summary = "Etat de l'API")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "application", appName,
                "timestamp", Instant.now().toString());
    }
}

package com.vms.backend.dto;

import java.time.Instant;

public record HealthResponse(
        String status,
        String message,
        Instant timestamp
) {
    public static HealthResponse up() {
        return new HealthResponse("UP", "Vehicle Management System Backend is running", Instant.now());
    }
}

package com.vms.backend.dto;

public record LoginResponse(
        String token,
        String email
) {}

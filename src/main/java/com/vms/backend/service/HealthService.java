package com.vms.backend.service;

import com.vms.backend.dto.HealthResponse;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    public HealthResponse getHealthStatus() {
        return HealthResponse.up();
    }
}

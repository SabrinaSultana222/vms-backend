package com.vms.backend.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum VehicleStatus {
    ACTIVE,
    INACTIVE,
    MAINTENANCE;

    @JsonCreator
    public static VehicleStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        for (VehicleStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status: '" + value + "'. Allowed values: ACTIVE, INACTIVE, MAINTENANCE");
    }
}

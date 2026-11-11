package com.vms.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VehicleRequest(
        @NotBlank(message = "Vehicle number is required")
        @Size(max = 50, message = "Vehicle number must not exceed 50 characters")
        String vehicleNumber,

        @NotBlank(message = "Company is required")
        @Size(max = 100, message = "Company must not exceed 100 characters")
        String company,

        @NotBlank(message = "Type is required")
        @Size(max = 50, message = "Type must not exceed 50 characters")
        String type,

        @NotBlank(message = "Brand is required")
        @Size(max = 100, message = "Brand must not exceed 100 characters")
        String brand,

        @NotBlank(message = "Model is required")
        @Size(max = 100, message = "Model must not exceed 100 characters")
        String model,

        @NotBlank(message = "Status is required")
        @Size(max = 50, message = "Status must not exceed 50 characters")
        String status
) {
    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getCompany() {
        return company;
    }

    public String getType() {
        return type;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getStatus() {
        return status;
    }
}

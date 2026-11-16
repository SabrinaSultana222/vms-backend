package com.vms.backend.dto;

import com.vms.backend.entity.Vehicle;
import com.vms.backend.entity.VehicleStatus;

public record VehicleResponse(
        Long id,
        String vehicleNumber,
        String company,
        String type,
        String brand,
        String model,
        VehicleStatus status
) {
    public static VehicleResponse fromEntity(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getVehicleNumber(),
                vehicle.getCompany(),
                vehicle.getType(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getStatus()
        );
    }

    public Long getId() {
        return id;
    }

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

    public VehicleStatus getStatus() {
        return status;
    }
}

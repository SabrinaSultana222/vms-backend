package com.vms.backend.exception;

public class DuplicateVehicleNumberException extends RuntimeException {

    private final String vehicleNumber;

    public DuplicateVehicleNumberException(String message) {
        super(message);
        this.vehicleNumber = null;
    }

    public DuplicateVehicleNumberException(String vehicleNumber, String message) {
        super(message);
        this.vehicleNumber = vehicleNumber;
    }

    public static DuplicateVehicleNumberException forVehicleNumber(String vehicleNumber) {
        return new DuplicateVehicleNumberException(
                vehicleNumber,
                "Vehicle already exists with vehicle number: " + vehicleNumber
        );
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }
}

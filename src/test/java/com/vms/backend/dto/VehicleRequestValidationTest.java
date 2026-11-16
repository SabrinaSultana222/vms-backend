package com.vms.backend.dto;

import com.vms.backend.entity.VehicleStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("valid VehicleRequest passes validation with zero violations")
    void validVehicleRequest_hasNoViolations() {
        VehicleRequest request = new VehicleRequest(
                "DHK-11-2233",
                "Acme Corp",
                "Truck",
                "Volvo",
                "FH16",
                VehicleStatus.ACTIVE
        );

        Set<ConstraintViolation<VehicleRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("VehicleRequest with blank vehicleNumber fails validation")
    void blankVehicleNumber_failsValidation() {
        VehicleRequest request = new VehicleRequest(
                "",
                "Acme Corp",
                "Truck",
                "Volvo",
                "FH16",
                VehicleStatus.ACTIVE
        );

        Set<ConstraintViolation<VehicleRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Vehicle number is required");
    }

    @Test
    @DisplayName("VehicleRequest with null fields fails validation")
    void nullFields_failValidation() {
        VehicleRequest request = new VehicleRequest(
                null,
                null,
                null,
                null,
                null,
                null
        );

        Set<ConstraintViolation<VehicleRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(6);
    }
}

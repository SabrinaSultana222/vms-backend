package com.vms.backend.exception;

import com.vms.backend.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleDuplicateVehicleNumberException returns HTTP 409 CONFLICT with error details")
    void handleDuplicateVehicleNumberException_returnsConflict() {
        DuplicateVehicleNumberException exception =
                DuplicateVehicleNumberException.forVehicleNumber("DHK-11-2233");
        HttpServletRequest request = new MockHttpServletRequest("POST", "/api/vehicles");

        ResponseEntity<ErrorResponse> response =
                handler.handleDuplicateVehicleNumberException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.getBody().error()).isEqualTo(HttpStatus.CONFLICT.getReasonPhrase());
        assertThat(response.getBody().message()).contains("DHK-11-2233");
        assertThat(response.getBody().path()).isEqualTo("/api/vehicles");
        assertThat(response.getBody().timestamp()).isNotNull();
    }
}

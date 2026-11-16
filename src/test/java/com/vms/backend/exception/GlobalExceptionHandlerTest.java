package com.vms.backend.exception;

import com.vms.backend.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    @Test
    @DisplayName("handleIllegalArgumentException returns HTTP 400 BAD_REQUEST with error details")
    void handleIllegalArgumentException_returnsBadRequest() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid sortBy field: 'unknown'");
        HttpServletRequest request = new MockHttpServletRequest("GET", "/api/vehicles");

        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgumentException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).contains("Invalid sortBy field: 'unknown'");
        assertThat(response.getBody().path()).isEqualTo("/api/vehicles");
    }

    @Test
    @DisplayName("handleHttpMessageNotReadableException returns HTTP 400 BAD_REQUEST with error details")
    void handleHttpMessageNotReadableException_returnsBadRequest() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "Invalid status value",
                new IllegalArgumentException("Invalid status: 'INVALID'"),
                new MockHttpInputMessage(new byte[0])
        );
        HttpServletRequest request = new MockHttpServletRequest("POST", "/api/vehicles");

        ResponseEntity<ErrorResponse> response =
                handler.handleHttpMessageNotReadableException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().message()).contains("Invalid status: 'INVALID'");
    }

    @Test
    @DisplayName("handleMethodArgumentTypeMismatchException returns HTTP 400 BAD_REQUEST with error details")
    void handleMethodArgumentTypeMismatchException_returnsBadRequest() {
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "INVALID",
                com.vms.backend.entity.VehicleStatus.class,
                "status",
                null,
                new IllegalArgumentException("Invalid status: 'INVALID'")
        );
        HttpServletRequest request = new MockHttpServletRequest("GET", "/api/vehicles");

        ResponseEntity<ErrorResponse> response =
                handler.handleMethodArgumentTypeMismatchException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().message()).contains("Invalid status: 'INVALID'");
    }
}

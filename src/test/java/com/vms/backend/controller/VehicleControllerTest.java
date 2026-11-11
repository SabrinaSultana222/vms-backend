package com.vms.backend.controller;

import com.vms.backend.dto.VehicleRequest;
import com.vms.backend.dto.VehicleResponse;
import com.vms.backend.exception.DuplicateVehicleNumberException;
import com.vms.backend.exception.GlobalExceptionHandler;
import com.vms.backend.exception.ResourceNotFoundException;
import com.vms.backend.service.VehicleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleController.class)
@Import(GlobalExceptionHandler.class)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleService vehicleService;

    private static final String VALID_VEHICLE_JSON = """
            {
              "vehicleNumber": "DHK-11-2233",
              "company": "Acme Corp",
              "type": "Truck",
              "brand": "Volvo",
              "model": "FH16",
              "status": "ACTIVE"
            }
            """;

    private final VehicleResponse sampleResponse = new VehicleResponse(
            1L,
            "DHK-11-2233",
            "Acme Corp",
            "Truck",
            "Volvo",
            "FH16",
            "ACTIVE"
    );

    @Nested
    @DisplayName("Create Vehicle Endpoints (POST /api/vehicles)")
    class CreateVehicleTests {

        @Test
        @DisplayName("POST /api/vehicles returns 201 Created and vehicle data on success")
        void createVehicle_success_returns201() throws Exception {
            when(vehicleService.createVehicle(any(VehicleRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/vehicles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_VEHICLE_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.vehicleNumber").value("DHK-11-2233"))
                    .andExpect(jsonPath("$.company").value("Acme Corp"))
                    .andExpect(jsonPath("$.type").value("Truck"))
                    .andExpect(jsonPath("$.brand").value("Volvo"))
                    .andExpect(jsonPath("$.model").value("FH16"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));

            verify(vehicleService).createVehicle(any(VehicleRequest.class));
        }

        @Test
        @DisplayName("POST /api/vehicles returns 400 Bad Request when request body is invalid")
        void createVehicle_invalidBody_returns400() throws Exception {
            String invalidJson = """
                    {
                      "vehicleNumber": "",
                      "company": "",
                      "type": null,
                      "brand": "Volvo",
                      "model": "FH16",
                      "status": "ACTIVE"
                    }
                    """;

            mockMvc.perform(post("/api/vehicles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.path").value("/api/vehicles"));
        }

        @Test
        @DisplayName("POST /api/vehicles returns 409 Conflict when duplicate vehicle number occurs")
        void createVehicle_duplicateVehicleNumber_returns409() throws Exception {
            when(vehicleService.createVehicle(any(VehicleRequest.class)))
                    .thenThrow(DuplicateVehicleNumberException.forVehicleNumber("DHK-11-2233"));

            mockMvc.perform(post("/api/vehicles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_VEHICLE_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value("Vehicle already exists with vehicle number: DHK-11-2233"))
                    .andExpect(jsonPath("$.path").value("/api/vehicles"));
        }
    }

    @Nested
    @DisplayName("List Vehicles Endpoints (GET /api/vehicles)")
    class ListVehiclesTests {

        @Test
        @DisplayName("GET /api/vehicles returns 200 OK and list of vehicles")
        void getAllVehicles_returnsList() throws Exception {
            when(vehicleService.getAllVehicles()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/vehicles")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].vehicleNumber").value("DHK-11-2233"))
                    .andExpect(jsonPath("$[0].company").value("Acme Corp"));

            verify(vehicleService).getAllVehicles();
        }

        @Test
        @DisplayName("GET /api/vehicles returns 200 OK and empty list when no vehicles exist")
        void getAllVehicles_empty_returnsEmptyList() throws Exception {
            when(vehicleService.getAllVehicles()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/vehicles")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(vehicleService).getAllVehicles();
        }
    }

    @Nested
    @DisplayName("Get Vehicle By ID Endpoints (GET /api/vehicles/{id})")
    class GetVehicleByIdTests {

        @Test
        @DisplayName("GET /api/vehicles/{id} returns 200 OK with vehicle data when found")
        void getVehicleById_found_returns200() throws Exception {
            when(vehicleService.getVehicleById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/vehicles/1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.vehicleNumber").value("DHK-11-2233"))
                    .andExpect(jsonPath("$.company").value("Acme Corp"))
                    .andExpect(jsonPath("$.type").value("Truck"))
                    .andExpect(jsonPath("$.brand").value("Volvo"))
                    .andExpect(jsonPath("$.model").value("FH16"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));

            verify(vehicleService).getVehicleById(1L);
        }

        @Test
        @DisplayName("GET /api/vehicles/{id} returns 404 Not Found when vehicle does not exist")
        void getVehicleById_notFound_returns404() throws Exception {
            when(vehicleService.getVehicleById(99L))
                    .thenThrow(new ResourceNotFoundException("Vehicle not found with id: 99"));

            mockMvc.perform(get("/api/vehicles/99")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Vehicle not found with id: 99"))
                    .andExpect(jsonPath("$.path").value("/api/vehicles/99"));

            verify(vehicleService).getVehicleById(99L);
        }
    }

    @Nested
    @DisplayName("Update Vehicle Endpoints (PUT /api/vehicles/{id})")
    class UpdateVehicleTests {

        @Test
        @DisplayName("PUT /api/vehicles/{id} returns 200 OK and updated vehicle on success")
        void updateVehicle_success_returns200() throws Exception {
            VehicleResponse updatedResponse = new VehicleResponse(
                    1L,
                    "DHK-11-2233",
                    "Updated Corp",
                    "Van",
                    "Mercedes",
                    "Sprinter",
                    "MAINTENANCE"
            );
            when(vehicleService.updateVehicle(eq(1L), any(VehicleRequest.class))).thenReturn(updatedResponse);

            mockMvc.perform(put("/api/vehicles/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_VEHICLE_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.company").value("Updated Corp"))
                    .andExpect(jsonPath("$.type").value("Van"))
                    .andExpect(jsonPath("$.brand").value("Mercedes"))
                    .andExpect(jsonPath("$.model").value("Sprinter"))
                    .andExpect(jsonPath("$.status").value("MAINTENANCE"));

            verify(vehicleService).updateVehicle(eq(1L), any(VehicleRequest.class));
        }

        @Test
        @DisplayName("PUT /api/vehicles/{id} returns 404 Not Found when vehicle does not exist")
        void updateVehicle_notFound_returns404() throws Exception {
            when(vehicleService.updateVehicle(eq(99L), any(VehicleRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Vehicle not found with id: 99"));

            mockMvc.perform(put("/api/vehicles/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_VEHICLE_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Vehicle not found with id: 99"))
                    .andExpect(jsonPath("$.path").value("/api/vehicles/99"));
        }

        @Test
        @DisplayName("PUT /api/vehicles/{id} returns 400 Bad Request when request body is invalid")
        void updateVehicle_invalidBody_returns400() throws Exception {
            String invalidJson = """
                    {
                      "vehicleNumber": "",
                      "company": "Acme Corp",
                      "type": "Truck",
                      "brand": "Volvo",
                      "model": "FH16",
                      "status": "ACTIVE"
                    }
                    """;

            mockMvc.perform(put("/api/vehicles/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.path").value("/api/vehicles/1"));
        }

        @Test
        @DisplayName("PUT /api/vehicles/{id} returns 409 Conflict when new vehicle number is already taken")
        void updateVehicle_duplicateVehicleNumber_returns409() throws Exception {
            when(vehicleService.updateVehicle(eq(1L), any(VehicleRequest.class)))
                    .thenThrow(DuplicateVehicleNumberException.forVehicleNumber("DHK-11-2233"));

            mockMvc.perform(put("/api/vehicles/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_VEHICLE_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value("Vehicle already exists with vehicle number: DHK-11-2233"))
                    .andExpect(jsonPath("$.path").value("/api/vehicles/1"));
        }
    }

    @Nested
    @DisplayName("Delete Vehicle Endpoints (DELETE /api/vehicles/{id})")
    class DeleteVehicleTests {

        @Test
        @DisplayName("DELETE /api/vehicles/{id} returns 204 No Content on success")
        void deleteVehicle_success_returns204() throws Exception {
            doNothing().when(vehicleService).deleteVehicle(1L);

            mockMvc.perform(delete("/api/vehicles/1"))
                    .andExpect(status().isNoContent());

            verify(vehicleService).deleteVehicle(1L);
        }

        @Test
        @DisplayName("DELETE /api/vehicles/{id} returns 404 Not Found when vehicle does not exist")
        void deleteVehicle_notFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Vehicle not found with id: 99"))
                    .when(vehicleService).deleteVehicle(99L);

            mockMvc.perform(delete("/api/vehicles/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Vehicle not found with id: 99"))
                    .andExpect(jsonPath("$.path").value("/api/vehicles/99"));

            verify(vehicleService).deleteVehicle(99L);
        }
    }
}

package com.vms.backend.service;

import com.vms.backend.dto.VehicleRequest;
import com.vms.backend.dto.VehicleResponse;
import com.vms.backend.entity.Vehicle;
import com.vms.backend.exception.DuplicateVehicleNumberException;
import com.vms.backend.exception.ResourceNotFoundException;
import com.vms.backend.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private VehicleRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = new VehicleRequest(
                "DHK-11-2233",
                "Acme Corp",
                "Truck",
                "Volvo",
                "FH16",
                "ACTIVE"
        );
    }

    @Nested
    @DisplayName("Duplicate Vehicle Number Rule Tests")
    class DuplicateVehicleNumberTests {

        @Test
        @DisplayName("createVehicle throws DuplicateVehicleNumberException when vehicleNumber already exists at service level")
        void createVehicle_whenVehicleNumberAlreadyExists_throwsDuplicateVehicleNumberException() {
            String duplicateNumber = "DHK-11-2233";
            when(vehicleRepository.existsByVehicleNumber(duplicateNumber)).thenReturn(true);

            assertThatThrownBy(() -> vehicleService.createVehicle(sampleRequest))
                    .isInstanceOf(DuplicateVehicleNumberException.class)
                    .hasMessageContaining(duplicateNumber);

            verify(vehicleRepository).existsByVehicleNumber(duplicateNumber);
            verify(vehicleRepository, never()).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("createVehicle throws DuplicateVehicleNumberException when database unique constraint violation occurs")
        void createVehicle_whenDatabaseConstraintViolationOccurs_throwsDuplicateVehicleNumberException() {
            when(vehicleRepository.existsByVehicleNumber("DHK-11-2233")).thenReturn(false);
            when(vehicleRepository.save(any(Vehicle.class)))
                    .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

            assertThatThrownBy(() -> vehicleService.createVehicle(sampleRequest))
                    .isInstanceOf(DuplicateVehicleNumberException.class)
                    .hasMessageContaining("DHK-11-2233");

            verify(vehicleRepository).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("createVehicle succeeds when vehicleNumber is unique")
        void createVehicle_whenVehicleNumberIsUnique_savesAndReturnsVehicleResponse() {
            when(vehicleRepository.existsByVehicleNumber("DHK-11-2233")).thenReturn(false);

            Vehicle savedEntity = new Vehicle(
                    1L,
                    "DHK-11-2233",
                    "Acme Corp",
                    "Truck",
                    "Volvo",
                    "FH16",
                    "ACTIVE"
            );
            when(vehicleRepository.save(any(Vehicle.class))).thenReturn(savedEntity);

            VehicleResponse response = vehicleService.createVehicle(sampleRequest);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.vehicleNumber()).isEqualTo("DHK-11-2233");
            assertThat(response.company()).isEqualTo("Acme Corp");
            assertThat(response.type()).isEqualTo("Truck");
            assertThat(response.brand()).isEqualTo("Volvo");
            assertThat(response.model()).isEqualTo("FH16");
            assertThat(response.status()).isEqualTo("ACTIVE");

            verify(vehicleRepository).existsByVehicleNumber("DHK-11-2233");
            verify(vehicleRepository).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("updateVehicle throws DuplicateVehicleNumberException when updated number belongs to another vehicle")
        void updateVehicle_whenNewVehicleNumberAlreadyExists_throwsDuplicateVehicleNumberException() {
            Vehicle existingVehicle = new Vehicle(
                    1L,
                    "OLD-00-1111",
                    "Acme Corp",
                    "Truck",
                    "Volvo",
                    "FH16",
                    "ACTIVE"
            );
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existingVehicle));
            when(vehicleRepository.existsByVehicleNumber("DHK-11-2233")).thenReturn(true);

            assertThatThrownBy(() -> vehicleService.updateVehicle(1L, sampleRequest))
                    .isInstanceOf(DuplicateVehicleNumberException.class)
                    .hasMessageContaining("DHK-11-2233");

            verify(vehicleRepository, never()).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("updateVehicle succeeds when vehicleNumber remains unchanged")
        void updateVehicle_whenVehicleNumberUnchanged_succeedsWithoutDuplicateError() {
            Vehicle existingVehicle = new Vehicle(
                    1L,
                    "DHK-11-2233",
                    "Old Corp",
                    "Van",
                    "Ford",
                    "Transit",
                    "INACTIVE"
            );
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existingVehicle));
            when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

            VehicleResponse response = vehicleService.updateVehicle(1L, sampleRequest);

            assertThat(response).isNotNull();
            assertThat(response.company()).isEqualTo("Acme Corp");
            assertThat(response.vehicleNumber()).isEqualTo("DHK-11-2233");
            verify(vehicleRepository, never()).existsByVehicleNumber(any());
            verify(vehicleRepository).save(existingVehicle);
        }

        @Test
        @DisplayName("updateVehicle throws DuplicateVehicleNumberException on database constraint violation")
        void updateVehicle_whenDatabaseConstraintViolationOccurs_throwsDuplicateVehicleNumberException() {
            Vehicle existingVehicle = new Vehicle(
                    1L,
                    "OLD-00-1111",
                    "Acme Corp",
                    "Truck",
                    "Volvo",
                    "FH16",
                    "ACTIVE"
            );
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existingVehicle));
            when(vehicleRepository.existsByVehicleNumber("DHK-11-2233")).thenReturn(false);
            when(vehicleRepository.save(any(Vehicle.class)))
                    .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

            assertThatThrownBy(() -> vehicleService.updateVehicle(1L, sampleRequest))
                    .isInstanceOf(DuplicateVehicleNumberException.class)
                    .hasMessageContaining("DHK-11-2233");
        }
    }

    @Nested
    @DisplayName("CRUD Operations Tests")
    class CrudOperationsTests {

        @Test
        @DisplayName("getVehicleById returns vehicle response when found")
        void getVehicleById_whenFound_returnsResponse() {
            Vehicle entity = new Vehicle(1L, "DHK-11-2233", "Acme", "Car", "Toyota", "Corolla", "ACTIVE");
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(entity));

            VehicleResponse response = vehicleService.getVehicleById(1L);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.vehicleNumber()).isEqualTo("DHK-11-2233");
        }

        @Test
        @DisplayName("getVehicleById throws ResourceNotFoundException when not found")
        void getVehicleById_whenNotFound_throwsResourceNotFoundException() {
            when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vehicleService.getVehicleById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("getVehicleByVehicleNumber throws ResourceNotFoundException when not found")
        void getVehicleByVehicleNumber_whenNotFound_throwsResourceNotFoundException() {
            when(vehicleRepository.findByVehicleNumber("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vehicleService.getVehicleByVehicleNumber("UNKNOWN"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("UNKNOWN");
        }

        @Test
        @DisplayName("deleteVehicle throws ResourceNotFoundException when vehicle does not exist")
        void deleteVehicle_whenNotFound_throwsResourceNotFoundException() {
            when(vehicleRepository.existsById(50L)).thenReturn(false);

            assertThatThrownBy(() -> vehicleService.deleteVehicle(50L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("50");

            verify(vehicleRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("deleteVehicle deletes vehicle when found")
        void deleteVehicle_whenFound_deletesVehicle() {
            when(vehicleRepository.existsById(1L)).thenReturn(true);

            vehicleService.deleteVehicle(1L);

            verify(vehicleRepository).deleteById(1L);
        }
    }
}

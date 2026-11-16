package com.vms.backend.service;

import com.vms.backend.dto.PageResponse;
import com.vms.backend.dto.VehicleRequest;
import com.vms.backend.dto.VehicleResponse;
import com.vms.backend.entity.Vehicle;
import com.vms.backend.entity.VehicleStatus;
import com.vms.backend.exception.DuplicateVehicleNumberException;
import com.vms.backend.exception.ResourceNotFoundException;
import com.vms.backend.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
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
                VehicleStatus.ACTIVE
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
                    VehicleStatus.ACTIVE
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
            assertThat(response.status()).isEqualTo(VehicleStatus.ACTIVE);

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
                    VehicleStatus.ACTIVE
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
                    VehicleStatus.INACTIVE
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
                    VehicleStatus.ACTIVE
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
            Vehicle entity = new Vehicle(1L, "DHK-11-2233", "Acme", "Car", "Toyota", "Corolla", VehicleStatus.ACTIVE);
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

    @Nested
    @DisplayName("Server-side List Handling Tests (getVehicles)")
    class GetVehiclesTests {

        @Test
        @DisplayName("getVehicles returns paged response with default pagination and sorting")
        void getVehicles_defaultParameters_returnsPagedResponse() {
            Vehicle vehicle = new Vehicle(1L, "DHK-11-2233", "Acme Corp", "Truck", "Volvo", "FH16", VehicleStatus.ACTIVE);
            Page<Vehicle> page = new PageImpl<>(List.of(vehicle), PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")), 1);

            when(vehicleRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            PageResponse<VehicleResponse> response = vehicleService.getVehicles(null, (String) null, null, "id", "asc", 0, 10);

            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(1);
            assertThat(response.content().get(0).vehicleNumber()).isEqualTo("DHK-11-2233");
            assertThat(response.page()).isEqualTo(0);
            assertThat(response.size()).isEqualTo(10);
            assertThat(response.totalElements()).isEqualTo(1L);
            assertThat(response.totalPages()).isEqualTo(1);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(vehicleRepository).findAll(any(Specification.class), pageableCaptor.capture());
            Pageable captured = pageableCaptor.getValue();
            assertThat(captured.getPageNumber()).isEqualTo(0);
            assertThat(captured.getPageSize()).isEqualTo(10);
            assertThat(captured.getSort().getOrderFor("id")).isNotNull();
            assertThat(captured.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.ASC);
        }

        @Test
        @DisplayName("getVehicles applies custom sorting correctly")
        void getVehicles_customSorting_passesCorrectSort() {
            Page<Vehicle> page = new PageImpl<>(List.of(), PageRequest.of(1, 20, Sort.by(Sort.Direction.DESC, "brand")), 0);
            when(vehicleRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            PageResponse<VehicleResponse> response = vehicleService.getVehicles(null, (String) null, null, "brand", "desc", 1, 20);

            assertThat(response.page()).isEqualTo(1);
            assertThat(response.size()).isEqualTo(20);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(vehicleRepository).findAll(any(Specification.class), pageableCaptor.capture());
            Pageable captured = pageableCaptor.getValue();
            assertThat(captured.getPageNumber()).isEqualTo(1);
            assertThat(captured.getPageSize()).isEqualTo(20);
            assertThat(captured.getSort().getOrderFor("brand").getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("getVehicles accepts all whitelisted sortBy fields")
        void getVehicles_allWhitelistedSortByFields_succeed() {
            List<String> allowedFields = List.of("vehicleNumber", "company", "brand", "model", "status", "id");
            for (String field : allowedFields) {
                Page<Vehicle> page = new PageImpl<>(List.of(), PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, field)), 0);
                when(vehicleRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

                PageResponse<VehicleResponse> response = vehicleService.getVehicles(null, (String) null, null, field, "asc", 0, 10);
                assertThat(response).isNotNull();
            }
        }

        @Test
        @DisplayName("getVehicles throws IllegalArgumentException when sortBy is not whitelisted")
        void getVehicles_invalidSortBy_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> vehicleService.getVehicles(null, (String) null, null, "password", "asc", 0, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid sortBy field: 'password'");

            verify(vehicleRepository, never()).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("getVehicles throws IllegalArgumentException when sortDir is invalid")
        void getVehicles_invalidSortDir_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> vehicleService.getVehicles(null, (String) null, null, "id", "sideways", 0, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid sortDir: 'sideways'");

            verify(vehicleRepository, never()).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("getVehicles throws IllegalArgumentException when page is negative")
        void getVehicles_negativePage_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> vehicleService.getVehicles(null, (String) null, null, "id", "asc", -1, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Page index must not be less than zero");

            verify(vehicleRepository, never()).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("getVehicles throws IllegalArgumentException when size is less than 1 or exceeds 100")
        void getVehicles_invalidSize_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> vehicleService.getVehicles(null, (String) null, null, "id", "asc", 0, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Page size must be between 1 and 100");

            assertThatThrownBy(() -> vehicleService.getVehicles(null, (String) null, null, "id", "asc", 0, 101))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Page size must be between 1 and 100");

            verify(vehicleRepository, never()).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("getVehicles throws IllegalArgumentException when status filter string is invalid")
        void getVehicles_invalidStatus_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> vehicleService.getVehicles(null, "UNKNOWN_STATUS", null, "id", "asc", 0, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid status: 'UNKNOWN_STATUS'");

            verify(vehicleRepository, never()).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("getVehicles successfully filters with valid status enum string")
        void getVehicles_validStatus_appliesFilter() {
            Page<Vehicle> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(vehicleRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            PageResponse<VehicleResponse> response = vehicleService.getVehicles("Volvo", "ACTIVE", "Truck", "id", "asc", 0, 10);

            assertThat(response).isNotNull();
            verify(vehicleRepository).findAll(any(Specification.class), any(Pageable.class));
        }
    }
}

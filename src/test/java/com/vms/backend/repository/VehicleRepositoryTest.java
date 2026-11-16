package com.vms.backend.repository;

import com.vms.backend.entity.Vehicle;
import com.vms.backend.entity.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class VehicleRepositoryTest {

    @Autowired
    private VehicleRepository vehicleRepository;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();

        Vehicle v1 = new Vehicle("DHK-11-2233", "Acme Corp", "Truck", "Volvo", "FH16", VehicleStatus.ACTIVE);
        Vehicle v2 = new Vehicle("CTG-22-3344", "Apex Logistics", "Van", "Mercedes", "Sprinter", VehicleStatus.MAINTENANCE);
        Vehicle v3 = new Vehicle("SYL-33-4455", "Acme Corp", "Sedan", "Toyota", "Corolla", VehicleStatus.INACTIVE);
        Vehicle v4 = new Vehicle("DHK-99-8877", "Global Movers", "Truck", "Scania", "R450", VehicleStatus.ACTIVE);

        vehicleRepository.save(v1);
        vehicleRepository.save(v2);
        vehicleRepository.save(v3);
        vehicleRepository.save(v4);
    }

    @Test
    @DisplayName("search query q matches vehicleNumber partially and case-insensitively")
    void search_matchesVehicleNumber() {
        Specification<Vehicle> spec = VehicleSpecification.withFilter("dhk-11", null, null);
        Page<Vehicle> result = vehicleRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getVehicleNumber()).isEqualTo("DHK-11-2233");
    }

    @Test
    @DisplayName("search query q matches company, brand, and model partially and case-insensitively")
    void search_matchesOtherFields() {
        // match company "acme"
        Specification<Vehicle> companySpec = VehicleSpecification.withFilter("acme", null, null);
        Page<Vehicle> companyResult = vehicleRepository.findAll(companySpec, PageRequest.of(0, 10));
        assertThat(companyResult.getContent()).hasSize(2);

        // match brand "toyota"
        Specification<Vehicle> brandSpec = VehicleSpecification.withFilter("toyota", null, null);
        Page<Vehicle> brandResult = vehicleRepository.findAll(brandSpec, PageRequest.of(0, 10));
        assertThat(brandResult.getContent()).hasSize(1);
        assertThat(brandResult.getContent().get(0).getBrand()).isEqualTo("Toyota");

        // match model "sprinter"
        Specification<Vehicle> modelSpec = VehicleSpecification.withFilter("sprinter", null, null);
        Page<Vehicle> modelResult = vehicleRepository.findAll(modelSpec, PageRequest.of(0, 10));
        assertThat(modelResult.getContent()).hasSize(1);
        assertThat(modelResult.getContent().get(0).getModel()).isEqualTo("Sprinter");
    }

    @Test
    @DisplayName("status filter returns only vehicles with specified VehicleStatus")
    void filter_byStatus() {
        Specification<Vehicle> spec = VehicleSpecification.withFilter(null, VehicleStatus.ACTIVE, null);
        Page<Vehicle> result = vehicleRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(v -> v.getStatus() == VehicleStatus.ACTIVE);
    }

    @Test
    @DisplayName("type filter matches type exactly and case-insensitively")
    void filter_byType() {
        Specification<Vehicle> spec = VehicleSpecification.withFilter(null, null, "truck");
        Page<Vehicle> result = vehicleRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(v -> "Truck".equalsIgnoreCase(v.getType()));
    }

    @Test
    @DisplayName("combined filters q, status, and type match intersection")
    void filter_combined() {
        Specification<Vehicle> spec = VehicleSpecification.withFilter("acme", VehicleStatus.ACTIVE, "truck");
        Page<Vehicle> result = vehicleRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getVehicleNumber()).isEqualTo("DHK-11-2233");
    }

    @Test
    @DisplayName("sorting and pagination works as expected")
    void paginationAndSorting() {
        Specification<Vehicle> allSpec = VehicleSpecification.withFilter(null, null, null);
        Page<Vehicle> page0 = vehicleRepository.findAll(
                allSpec,
                PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "vehicleNumber"))
        );
        assertThat(page0.getContent()).hasSize(2);
        assertThat(page0.getTotalElements()).isEqualTo(4);
        assertThat(page0.getTotalPages()).isEqualTo(2);
        assertThat(page0.getContent().get(0).getVehicleNumber()).isEqualTo("CTG-22-3344");
        assertThat(page0.getContent().get(1).getVehicleNumber()).isEqualTo("DHK-11-2233");

        Page<Vehicle> page1 = vehicleRepository.findAll(
                allSpec,
                PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "vehicleNumber"))
        );
        assertThat(page1.getContent()).hasSize(2);
        assertThat(page1.getContent().get(0).getVehicleNumber()).isEqualTo("DHK-99-8877");
        assertThat(page1.getContent().get(1).getVehicleNumber()).isEqualTo("SYL-33-4455");
    }
}

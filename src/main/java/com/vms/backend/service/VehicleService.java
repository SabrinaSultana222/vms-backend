package com.vms.backend.service;

import com.vms.backend.dto.VehicleRequest;
import com.vms.backend.dto.VehicleResponse;
import com.vms.backend.entity.Vehicle;
import com.vms.backend.exception.DuplicateVehicleNumberException;
import com.vms.backend.exception.ResourceNotFoundException;
import com.vms.backend.repository.VehicleRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public VehicleResponse createVehicle(@Valid VehicleRequest request) {
        String vehicleNumber = request.vehicleNumber() != null ? request.vehicleNumber().trim() : null;

        if (vehicleRepository.existsByVehicleNumber(vehicleNumber)) {
            throw DuplicateVehicleNumberException.forVehicleNumber(vehicleNumber);
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNumber(vehicleNumber);
        vehicle.setCompany(request.company());
        vehicle.setType(request.type());
        vehicle.setBrand(request.brand());
        vehicle.setModel(request.model());
        vehicle.setStatus(request.status());

        try {
            Vehicle savedVehicle = vehicleRepository.save(vehicle);
            return VehicleResponse.fromEntity(savedVehicle);
        } catch (DataIntegrityViolationException ex) {
            throw DuplicateVehicleNumberException.forVehicleNumber(vehicleNumber);
        }
    }

    public VehicleResponse create(@Valid VehicleRequest request) {
        return createVehicle(request);
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
        return VehicleResponse.fromEntity(vehicle);
    }

    public VehicleResponse getById(Long id) {
        return getVehicleById(id);
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicleByVehicleNumber(String vehicleNumber) {
        String trimmedNumber = vehicleNumber != null ? vehicleNumber.trim() : null;
        Vehicle vehicle = vehicleRepository.findByVehicleNumber(trimmedNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with vehicle number: " + trimmedNumber));
        return VehicleResponse.fromEntity(vehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(VehicleResponse::fromEntity)
                .toList();
    }

    public List<VehicleResponse> getAll() {
        return getAllVehicles();
    }

    @Transactional
    public VehicleResponse updateVehicle(Long id, @Valid VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        String newVehicleNumber = request.vehicleNumber() != null ? request.vehicleNumber().trim() : null;

        if (!vehicle.getVehicleNumber().equals(newVehicleNumber)
                && vehicleRepository.existsByVehicleNumber(newVehicleNumber)) {
            throw DuplicateVehicleNumberException.forVehicleNumber(newVehicleNumber);
        }

        vehicle.setVehicleNumber(newVehicleNumber);
        vehicle.setCompany(request.company());
        vehicle.setType(request.type());
        vehicle.setBrand(request.brand());
        vehicle.setModel(request.model());
        vehicle.setStatus(request.status());

        try {
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return VehicleResponse.fromEntity(updatedVehicle);
        } catch (DataIntegrityViolationException ex) {
            throw DuplicateVehicleNumberException.forVehicleNumber(newVehicleNumber);
        }
    }

    @Transactional
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicle not found with id: " + id);
        }
        vehicleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByVehicleNumber(String vehicleNumber) {
        String trimmedNumber = vehicleNumber != null ? vehicleNumber.trim() : null;
        return vehicleRepository.existsByVehicleNumber(trimmedNumber);
    }
}

package com.musa.stark_group_vms.service.impl;

import com.musa.stark_group_vms.dto.CreateVehicleRequest;
import com.musa.stark_group_vms.dto.PageResponse;
import com.musa.stark_group_vms.dto.UpdateVehicleRequest;
import com.musa.stark_group_vms.dto.VehicleResponse;
import com.musa.stark_group_vms.entity.Vehicle;
import com.musa.stark_group_vms.enums.FuelType;
import com.musa.stark_group_vms.enums.VehicleStatus;
import com.musa.stark_group_vms.enums.VehicleType;
import com.musa.stark_group_vms.exception.DuplicateResourceException;
import com.musa.stark_group_vms.exception.InvalidOperationException;
import com.musa.stark_group_vms.exception.ResourceNotFoundException;
import com.musa.stark_group_vms.mapper.VehicleMapper;
import com.musa.stark_group_vms.repository.VehicleRepository;
import com.musa.stark_group_vms.service.VehicleService;
import com.musa.stark_group_vms.specification.VehicleSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private static final Map<String, String> ALLOWED_SORT_FIELDS = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("vehiclenumber", "vehicleNumber"),
            Map.entry("vehicle_number", "vehicleNumber"),
            Map.entry("vehicletype", "vehicleType"),
            Map.entry("vehicle_type", "vehicleType"),
            Map.entry("brand", "brand"),
            Map.entry("model", "model"),
            Map.entry("manufacturingyear", "manufacturingYear"),
            Map.entry("manufacturing_year", "manufacturingYear"),
            Map.entry("color", "color"),
            Map.entry("fueltype", "fuelType"),
            Map.entry("fuel_type", "fuelType"),
            Map.entry("seatingcapacity", "seatingCapacity"),
            Map.entry("seating_capacity", "seatingCapacity"),
            Map.entry("drivername", "driverName"),
            Map.entry("driver_name", "driverName"),
            Map.entry("driverphone", "driverPhone"),
            Map.entry("driver_phone", "driverPhone"),
            Map.entry("status", "status"),
            Map.entry("createdat", "createdAt"),
            Map.entry("created_at", "createdAt"),
            Map.entry("updatedat", "updatedAt"),
            Map.entry("updated_at", "updatedAt")
    );

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;

    @Override
    @Transactional
    public VehicleResponse createVehicle(CreateVehicleRequest request) {
        String normalizedNumber = normalizeVehicleNumber(request.vehicleNumber());

        if (vehicleRepository.existsByVehicleNumber(normalizedNumber)) {
            throw new DuplicateResourceException("A vehicle with this vehicle number already exists.");
        }

        Vehicle vehicle = vehicleMapper.toEntity(request, normalizedNumber);
        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public VehicleResponse updateVehicle(Long id, UpdateVehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        String normalizedNumber = normalizeVehicleNumber(request.vehicleNumber());

        if (vehicleRepository.existsByVehicleNumberAndIdNot(normalizedNumber, id)) {
            throw new DuplicateResourceException("A vehicle with this vehicle number already exists.");
        }

        vehicleMapper.updateEntity(vehicle, request, normalizedNumber);
        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
        vehicleRepository.delete(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VehicleResponse> getVehicles(
            String search,
            VehicleType vehicleType,
            FuelType fuelType,
            VehicleStatus status,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        validatePagination(page, size);
        String resolvedSortBy = resolveSortField(sortBy);
        Sort.Direction sortDirection = resolveSortDirection(direction);

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, resolvedSortBy));
        Specification<Vehicle> spec = VehicleSpecification.withFilters(search, vehicleType, fuelType, status);

        Page<Vehicle> vehiclePage = vehicleRepository.findAll(spec, pageable);
        Page<VehicleResponse> responsePage = vehiclePage.map(vehicleMapper::toResponse);

        return PageResponse.from(responsePage);
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new InvalidOperationException("Page number must not be less than zero.");
        }
        if (size < 1) {
            throw new InvalidOperationException("Page size must be at least 1.");
        }
        if (size > 100) {
            throw new InvalidOperationException("Page size must not exceed 100.");
        }
    }

    private String resolveSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "createdAt";
        }
        String normalized = sortBy.trim().toLowerCase();
        String resolved = ALLOWED_SORT_FIELDS.get(normalized);
        if (resolved == null) {
            throw new InvalidOperationException("Invalid sortBy field: '" + sortBy + "'. Allowed fields are: "
                    + "id, vehicleNumber, vehicleType, brand, model, manufacturingYear, color, fuelType, seatingCapacity, driverName, driverPhone, status, createdAt, updatedAt.");
        }
        return resolved;
    }

    private Sort.Direction resolveSortDirection(String direction) {
        if (direction == null || direction.trim().isEmpty()) {
            return Sort.Direction.DESC;
        }
        String trimmed = direction.trim();
        if (trimmed.equalsIgnoreCase("ASC")) {
            return Sort.Direction.ASC;
        } else if (trimmed.equalsIgnoreCase("DESC")) {
            return Sort.Direction.DESC;
        } else {
            throw new InvalidOperationException("Invalid sort direction: '" + direction + "'. Allowed values are: ASC, DESC.");
        }
    }

    private String normalizeVehicleNumber(String vehicleNumber) {
        return vehicleNumber.trim().toLowerCase();
    }
}

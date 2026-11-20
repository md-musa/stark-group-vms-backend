package com.musa.stark_group_vms.service;

import com.musa.stark_group_vms.dto.CreateVehicleRequest;
import com.musa.stark_group_vms.dto.PageResponse;
import com.musa.stark_group_vms.dto.VehicleResponse;
import com.musa.stark_group_vms.enums.FuelType;
import com.musa.stark_group_vms.enums.VehicleStatus;
import com.musa.stark_group_vms.enums.VehicleType;

public interface VehicleService {

    /**
     * Creates a new vehicle.
     *
     * @param request the creation request DTO
     * @return the created vehicle as a response DTO
     * @throws com.musa.stark_group_vms.exception.DuplicateResourceException
     *         if a vehicle with the same vehicle number already exists
     */
    VehicleResponse createVehicle(CreateVehicleRequest request);

    /**
     * Retrieves a paginated list of vehicles filtered by the specified criteria.
     *
     * @param search optional keyword to match vehicle number, brand, model, or driver name
     * @param vehicleType optional vehicle type filter
     * @param fuelType optional fuel type filter
     * @param status optional vehicle status filter
     * @param page zero-based page index (must be >= 0)
     * @param size page size (must be between 1 and 100)
     * @param sortBy field name to sort by (default: createdAt)
     * @param direction sort direction (ASC or DESC, default: DESC)
     * @return paginated response containing matching vehicle DTOs
     */
    PageResponse<VehicleResponse> getVehicles(
            String search,
            VehicleType vehicleType,
            FuelType fuelType,
            VehicleStatus status,
            int page,
            int size,
            String sortBy,
            String direction
    );
}

package com.musa.stark_group_vms.service;

import com.musa.stark_group_vms.dto.CreateVehicleRequest;
import com.musa.stark_group_vms.dto.VehicleResponse;

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
}

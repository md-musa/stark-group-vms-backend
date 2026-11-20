package com.musa.stark_group_vms.service.impl;

import com.musa.stark_group_vms.dto.CreateVehicleRequest;
import com.musa.stark_group_vms.dto.VehicleResponse;
import com.musa.stark_group_vms.entity.Vehicle;
import com.musa.stark_group_vms.exception.DuplicateResourceException;
import com.musa.stark_group_vms.mapper.VehicleMapper;
import com.musa.stark_group_vms.repository.VehicleRepository;
import com.musa.stark_group_vms.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

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

    private String normalizeVehicleNumber(String vehicleNumber) {
        return vehicleNumber.trim().toLowerCase();
    }
}

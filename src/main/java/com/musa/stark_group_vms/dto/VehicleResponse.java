package com.musa.stark_group_vms.dto;

import com.musa.stark_group_vms.enums.FuelType;
import com.musa.stark_group_vms.enums.VehicleStatus;
import com.musa.stark_group_vms.enums.VehicleType;

import java.time.LocalDateTime;

public record VehicleResponse(
        Long id,
        String vehicleNumber,
        VehicleType vehicleType,
        String brand,
        String model,
        Integer manufacturingYear,
        String color,
        FuelType fuelType,
        Integer seatingCapacity,
        String driverName,
        String driverPhone,
        VehicleStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

package com.musa.stark_group_vms.mapper;

import com.musa.stark_group_vms.dto.CreateVehicleRequest;
import com.musa.stark_group_vms.dto.UpdateVehicleRequest;
import com.musa.stark_group_vms.dto.VehicleResponse;
import com.musa.stark_group_vms.entity.Vehicle;
import com.musa.stark_group_vms.enums.VehicleStatus;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    public Vehicle toEntity(CreateVehicleRequest request, String vehicleNumber) {
        return Vehicle.builder()
                .vehicleNumber(vehicleNumber)
                .vehicleType(request.vehicleType())
                .brand(request.brand())
                .model(request.model())
                .manufacturingYear(request.manufacturingYear())
                .color(request.color())
                .fuelType(request.fuelType())
                .seatingCapacity(request.seatingCapacity())
                .driverName(request.driverName())
                .driverPhone(request.driverPhone())
                .status(VehicleStatus.AVAILABLE)
                .build();
    }

    public void updateEntity(Vehicle vehicle, UpdateVehicleRequest request, String vehicleNumber) {
        vehicle.setVehicleNumber(vehicleNumber);
        vehicle.setVehicleType(request.vehicleType());
        vehicle.setBrand(request.brand());
        vehicle.setModel(request.model());
        vehicle.setManufacturingYear(request.manufacturingYear());
        vehicle.setColor(request.color());
        vehicle.setFuelType(request.fuelType());
        vehicle.setSeatingCapacity(request.seatingCapacity());
        vehicle.setDriverName(request.driverName());
        vehicle.setDriverPhone(request.driverPhone());
    }

    public VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getVehicleNumber(),
                vehicle.getVehicleType(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getManufacturingYear(),
                vehicle.getColor(),
                vehicle.getFuelType(),
                vehicle.getSeatingCapacity(),
                vehicle.getDriverName(),
                vehicle.getDriverPhone(),
                vehicle.getStatus(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }
}

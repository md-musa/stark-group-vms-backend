package com.musa.stark_group_vms.integration;

import com.musa.stark_group_vms.entity.Vehicle;
import com.musa.stark_group_vms.enums.FuelType;
import com.musa.stark_group_vms.enums.VehicleStatus;
import com.musa.stark_group_vms.enums.VehicleType;
import com.musa.stark_group_vms.exception.ResourceNotFoundException;
import com.musa.stark_group_vms.repository.VehicleRepository;
import com.musa.stark_group_vms.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class VehicleDeleteIntegrationTest {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleRepository vehicleRepository;

    private Vehicle vehicle1;
    private Vehicle vehicle2;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();

        vehicle1 = vehicleRepository.save(Vehicle.builder()
                .vehicleNumber("dhaka-metro-ga-11-1001")
                .vehicleType(VehicleType.MICROBUS)
                .brand("Toyota")
                .model("Hiace")
                .manufacturingYear(2020)
                .color("White")
                .fuelType(FuelType.OCTANE)
                .seatingCapacity(12)
                .driverName("Karim Uddin")
                .driverPhone("01711111111")
                .status(VehicleStatus.AVAILABLE)
                .build());

        vehicle2 = vehicleRepository.save(Vehicle.builder()
                .vehicleNumber("chattogram-metro-ha-22-2002")
                .vehicleType(VehicleType.SUV)
                .brand("Mitsubishi")
                .model("Pajero")
                .manufacturingYear(2018)
                .color("Black")
                .fuelType(FuelType.DIESEL)
                .seatingCapacity(7)
                .driverName("Rahim Ali")
                .driverPhone("01822222222")
                .status(VehicleStatus.MAINTENANCE)
                .build());
    }

    @Test
    void testDeleteExistingVehicle_Returns204AndRecordGone() {
        Long id = vehicle1.getId();

        vehicleService.deleteVehicle(id);

        assertThat(vehicleRepository.existsById(id)).isFalse();
    }

    @Test
    void testDeleteNonExistentVehicle_ThrowsResourceNotFoundException() {
        assertThatThrownBy(() -> vehicleService.deleteVehicle(999999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vehicle not found with id: 999999");
    }

    @Test
    void testDeleteSameVehicleTwice_SecondCallThrows404() {
        Long id = vehicle1.getId();
        vehicleService.deleteVehicle(id);

        assertThatThrownBy(() -> vehicleService.deleteVehicle(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vehicle not found with id: " + id);
    }

    @Test
    void testDeleteOneVehicle_OtherVehiclesUnaffected() {
        Long id1 = vehicle1.getId();
        Long id2 = vehicle2.getId();

        vehicleService.deleteVehicle(id1);

        assertThat(vehicleRepository.existsById(id1)).isFalse();
        assertThat(vehicleRepository.existsById(id2)).isTrue();

        Vehicle surviving = vehicleRepository.findById(id2).orElseThrow();
        assertThat(surviving.getVehicleNumber()).isEqualTo("chattogram-metro-ha-22-2002");
        assertThat(surviving.getBrand()).isEqualTo("Mitsubishi");
    }
}

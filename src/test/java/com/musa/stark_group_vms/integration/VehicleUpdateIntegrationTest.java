package com.musa.stark_group_vms.integration;

import com.musa.stark_group_vms.dto.UpdateVehicleRequest;
import com.musa.stark_group_vms.dto.VehicleResponse;
import com.musa.stark_group_vms.entity.Vehicle;
import com.musa.stark_group_vms.enums.FuelType;
import com.musa.stark_group_vms.enums.VehicleStatus;
import com.musa.stark_group_vms.enums.VehicleType;
import com.musa.stark_group_vms.exception.DuplicateResourceException;
import com.musa.stark_group_vms.exception.ResourceNotFoundException;
import com.musa.stark_group_vms.repository.VehicleRepository;
import com.musa.stark_group_vms.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class VehicleUpdateIntegrationTest {

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
                .status(VehicleStatus.IN_USE)
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
    void testUpdateExistingVehicleSuccessfully() {
        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "dhaka-metro-ga-11-1001",
                VehicleType.VAN,
                "Toyota Updated",
                "Hiace Grand",
                2022,
                "Silver",
                FuelType.PETROL,
                14,
                "Karim Ahmed",
                "01799999999"
        );

        VehicleResponse response = vehicleService.updateVehicle(vehicle1.getId(), request);

        assertThat(response.id()).isEqualTo(vehicle1.getId());
        assertThat(response.vehicleNumber()).isEqualTo("dhaka-metro-ga-11-1001");
        assertThat(response.vehicleType()).isEqualTo(VehicleType.VAN);
        assertThat(response.brand()).isEqualTo("Toyota Updated");
        assertThat(response.model()).isEqualTo("Hiace Grand");
        assertThat(response.manufacturingYear()).isEqualTo(2022);
        assertThat(response.color()).isEqualTo("Silver");
        assertThat(response.fuelType()).isEqualTo(FuelType.PETROL);
        assertThat(response.seatingCapacity()).isEqualTo(14);
        assertThat(response.driverName()).isEqualTo("Karim Ahmed");
        assertThat(response.driverPhone()).isEqualTo("01799999999");
        assertThat(response.status()).isEqualTo(VehicleStatus.IN_USE);
    }

    @Test
    void testUpdateNonExistentVehicle_Throws404() {
        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "dhaka-metro-ga-11-9999",
                VehicleType.CAR,
                "Honda",
                "Civic",
                2021,
                "Blue",
                FuelType.OCTANE,
                5,
                "Driver Name",
                "01700000000"
        );

        assertThatThrownBy(() -> vehicleService.updateVehicle(999999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vehicle not found with id: 999999");
    }

    @Test
    void testUpdateRetainingSameVehicleNumber_Success() {
        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "DHAKA-METRO-GA-11-1001", // case-insensitive check
                VehicleType.MICROBUS,
                "Toyota",
                "Hiace",
                2021,
                "White Pearl",
                FuelType.OCTANE,
                12,
                "Karim Uddin",
                "01711111111"
        );

        VehicleResponse response = vehicleService.updateVehicle(vehicle1.getId(), request);

        assertThat(response.id()).isEqualTo(vehicle1.getId());
        assertThat(response.vehicleNumber()).isEqualTo("dhaka-metro-ga-11-1001");
        assertThat(response.color()).isEqualTo("White Pearl");
        assertThat(response.manufacturingYear()).isEqualTo(2021);
    }

    @Test
    void testUpdateDuplicateVehicleNumber_Throws409() {
        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "chattogram-metro-ha-22-2002", // vehicle2's number
                VehicleType.MICROBUS,
                "Toyota",
                "Hiace",
                2020,
                "White",
                FuelType.OCTANE,
                12,
                "Karim Uddin",
                "01711111111"
        );

        assertThatThrownBy(() -> vehicleService.updateVehicle(vehicle1.getId(), request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("A vehicle with this vehicle number already exists.");
    }

    @Test
    void testUpdateCreatedAtUnchangedAndUpdatedAtChanged() {
        LocalDateTime initialCreatedAt = vehicle1.getCreatedAt();
        LocalDateTime initialUpdatedAt = vehicle1.getUpdatedAt();

        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "dhaka-metro-ga-11-1001",
                VehicleType.MICROBUS,
                "Toyota",
                "Hiace Prime",
                2020,
                "Gold",
                FuelType.OCTANE,
                12,
                "Karim Uddin",
                "01711111111"
        );

        VehicleResponse response = vehicleService.updateVehicle(vehicle1.getId(), request);

        assertThat(response.createdAt()).isEqualTo(initialCreatedAt);
        assertThat(response.updatedAt()).isNotNull();
    }

    @Test
    void testUpdatePreservesStatus() {
        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "dhaka-metro-ga-11-1001",
                VehicleType.MICROBUS,
                "Toyota",
                "Hiace Super",
                2020,
                "Blue",
                FuelType.OCTANE,
                12,
                "Karim Uddin",
                "01711111111"
        );

        VehicleResponse response = vehicleService.updateVehicle(vehicle1.getId(), request);

        assertThat(response.status()).isEqualTo(VehicleStatus.IN_USE);
    }

    @Test
    void testDatabaseContainsUpdatedValues() {
        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "dhaka-metro-ga-11-1001",
                VehicleType.VAN,
                "Toyota Updated in DB",
                "Hiace DB",
                2023,
                "Navy Blue",
                FuelType.HYBRID,
                10,
                "New Driver",
                "01755555555"
        );

        vehicleService.updateVehicle(vehicle1.getId(), request);

        Vehicle inDb = vehicleRepository.findById(vehicle1.getId()).orElseThrow();
        assertThat(inDb.getBrand()).isEqualTo("Toyota Updated in DB");
        assertThat(inDb.getModel()).isEqualTo("Hiace DB");
        assertThat(inDb.getVehicleType()).isEqualTo(VehicleType.VAN);
        assertThat(inDb.getManufacturingYear()).isEqualTo(2023);
        assertThat(inDb.getColor()).isEqualTo("Navy Blue");
        assertThat(inDb.getFuelType()).isEqualTo(FuelType.HYBRID);
        assertThat(inDb.getSeatingCapacity()).isEqualTo(10);
        assertThat(inDb.getDriverName()).isEqualTo("New Driver");
        assertThat(inDb.getDriverPhone()).isEqualTo("01755555555");
        assertThat(inDb.getStatus()).isEqualTo(VehicleStatus.IN_USE);
    }
}

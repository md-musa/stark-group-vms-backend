package com.musa.stark_group_vms.service;

import com.musa.stark_group_vms.dto.CreateVehicleRequest;
import com.musa.stark_group_vms.dto.VehicleResponse;
import com.musa.stark_group_vms.entity.Vehicle;
import com.musa.stark_group_vms.enums.FuelType;
import com.musa.stark_group_vms.enums.VehicleStatus;
import com.musa.stark_group_vms.enums.VehicleType;
import com.musa.stark_group_vms.exception.DuplicateResourceException;
import com.musa.stark_group_vms.mapper.VehicleMapper;
import com.musa.stark_group_vms.repository.VehicleRepository;
import com.musa.stark_group_vms.service.impl.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleMapper vehicleMapper;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private CreateVehicleRequest sampleRequest;
    private Vehicle sampleVehicle;
    private VehicleResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleRequest = new CreateVehicleRequest(
                "dhaka-metro-ga-11-2345",
                VehicleType.MICROBUS,
                "Toyota",
                "Hiace",
                2020,
                "White",
                FuelType.OCTANE,
                12,
                "Karim Uddin",
                "01712345678"
        );

        sampleVehicle = new Vehicle();
        sampleVehicle.setId(1L);
        sampleVehicle.setVehicleNumber("dhaka-metro-ga-11-2345");
        sampleVehicle.setVehicleType(VehicleType.MICROBUS);
        sampleVehicle.setBrand("Toyota");
        sampleVehicle.setModel("Hiace");
        sampleVehicle.setManufacturingYear(2020);
        sampleVehicle.setColor("White");
        sampleVehicle.setFuelType(FuelType.OCTANE);
        sampleVehicle.setSeatingCapacity(12);
        sampleVehicle.setDriverName("Karim Uddin");
        sampleVehicle.setDriverPhone("01712345678");
        sampleVehicle.setStatus(VehicleStatus.AVAILABLE);
        sampleVehicle.setCreatedAt(LocalDateTime.now());
        sampleVehicle.setUpdatedAt(LocalDateTime.now());

        sampleResponse = new VehicleResponse(
                1L,
                "dhaka-metro-ga-11-2345",
                VehicleType.MICROBUS,
                "Toyota",
                "Hiace",
                2020,
                "White",
                FuelType.OCTANE,
                12,
                "Karim Uddin",
                "01712345678",
                VehicleStatus.AVAILABLE,
                sampleVehicle.getCreatedAt(),
                sampleVehicle.getUpdatedAt()
        );
    }

    @Test
    void createVehicle_Success() {
        when(vehicleRepository.existsByVehicleNumber("dhaka-metro-ga-11-2345")).thenReturn(false);
        when(vehicleMapper.toEntity(eq(sampleRequest), eq("dhaka-metro-ga-11-2345"))).thenReturn(sampleVehicle);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(sampleVehicle);
        when(vehicleMapper.toResponse(sampleVehicle)).thenReturn(sampleResponse);

        VehicleResponse result = vehicleService.createVehicle(sampleRequest);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.vehicleNumber()).isEqualTo("dhaka-metro-ga-11-2345");
        assertThat(result.status()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    @Test
    void createVehicle_DuplicateVehicleNumber_ThrowsException() {
        when(vehicleRepository.existsByVehicleNumber("dhaka-metro-ga-11-2345")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.createVehicle(sampleRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("A vehicle with this vehicle number already exists.");
    }
}

package com.musa.stark_group_vms.service;

import com.musa.stark_group_vms.dto.CreateVehicleRequest;
import com.musa.stark_group_vms.dto.PageResponse;
import com.musa.stark_group_vms.dto.VehicleResponse;
import com.musa.stark_group_vms.entity.Vehicle;
import com.musa.stark_group_vms.enums.FuelType;
import com.musa.stark_group_vms.enums.VehicleStatus;
import com.musa.stark_group_vms.enums.VehicleType;
import com.musa.stark_group_vms.exception.DuplicateResourceException;
import com.musa.stark_group_vms.exception.InvalidOperationException;
import com.musa.stark_group_vms.mapper.VehicleMapper;
import com.musa.stark_group_vms.repository.VehicleRepository;
import com.musa.stark_group_vms.service.impl.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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

    @Test
    void getVehicles_DefaultParameters_Success() {
        Page<Vehicle> vehiclePage = new PageImpl<>(List.of(sampleVehicle), org.springframework.data.domain.PageRequest.of(0, 10), 1L);
        when(vehicleRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(vehiclePage);
        when(vehicleMapper.toResponse(sampleVehicle)).thenReturn(sampleResponse);

        PageResponse<VehicleResponse> result = vehicleService.getVehicles(
                null, null, null, null, 0, 10, "createdAt", "DESC"
        );

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.content().getFirst().vehicleNumber()).isEqualTo("dhaka-metro-ga-11-2345");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(vehicleRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getPageNumber()).isEqualTo(0);
        assertThat(capturedPageable.getPageSize()).isEqualTo(10);
        assertThat(capturedPageable.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(capturedPageable.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getVehicles_EmptyResult_Success() {
        Page<Vehicle> emptyPage = new PageImpl<>(Collections.emptyList(), org.springframework.data.domain.PageRequest.of(0, 10), 0L);
        when(vehicleRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        PageResponse<VehicleResponse> result = vehicleService.getVehicles(
                null, null, null, null, 0, 10, "createdAt", "DESC"
        );

        assertThat(result).isNotNull();
        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0L);
        assertThat(result.totalPages()).isEqualTo(0);
    }

    @Test
    void getVehicles_CustomSorting_ResolvesFieldAndDirection() {
        Page<Vehicle> vehiclePage = new PageImpl<>(List.of(sampleVehicle));
        when(vehicleRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(vehiclePage);
        when(vehicleMapper.toResponse(sampleVehicle)).thenReturn(sampleResponse);

        PageResponse<VehicleResponse> result = vehicleService.getVehicles(
                "toyota", VehicleType.MICROBUS, FuelType.OCTANE, VehicleStatus.AVAILABLE,
                2, 20, "brand", "ASC"
        );

        assertThat(result).isNotNull();
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(vehicleRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getPageNumber()).isEqualTo(2);
        assertThat(capturedPageable.getPageSize()).isEqualTo(20);
        assertThat(capturedPageable.getSort().getOrderFor("brand")).isNotNull();
        assertThat(capturedPageable.getSort().getOrderFor("brand").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getVehicles_SnakeCaseSortField_ResolvesToCamelCase() {
        Page<Vehicle> vehiclePage = new PageImpl<>(List.of(sampleVehicle));
        when(vehicleRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(vehiclePage);
        when(vehicleMapper.toResponse(sampleVehicle)).thenReturn(sampleResponse);

        vehicleService.getVehicles(
                null, null, null, null,
                0, 10, "manufacturing_year", "asc"
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(vehicleRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getSort().getOrderFor("manufacturingYear")).isNotNull();
        assertThat(capturedPageable.getSort().getOrderFor("manufacturingYear").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getVehicles_NegativePage_ThrowsInvalidOperationException() {
        assertThatThrownBy(() -> vehicleService.getVehicles(
                null, null, null, null, -1, 10, "createdAt", "DESC"
        ))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Page number must not be less than zero.");
    }

    @Test
    void getVehicles_InvalidSizeZero_ThrowsInvalidOperationException() {
        assertThatThrownBy(() -> vehicleService.getVehicles(
                null, null, null, null, 0, 0, "createdAt", "DESC"
        ))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Page size must be at least 1.");
    }

    @Test
    void getVehicles_InvalidSizeGreaterThan100_ThrowsInvalidOperationException() {
        assertThatThrownBy(() -> vehicleService.getVehicles(
                null, null, null, null, 0, 101, "createdAt", "DESC"
        ))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Page size must not exceed 100.");
    }

    @Test
    void getVehicles_InvalidSortByField_ThrowsInvalidOperationException() {
        assertThatThrownBy(() -> vehicleService.getVehicles(
                null, null, null, null, 0, 10, "secretField", "DESC"
        ))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Invalid sortBy field: 'secretField'");
    }

    @Test
    void getVehicles_InvalidSortDirection_ThrowsInvalidOperationException() {
        assertThatThrownBy(() -> vehicleService.getVehicles(
                null, null, null, null, 0, 10, "createdAt", "DIAGONAL"
        ))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Invalid sort direction: 'DIAGONAL'");
    }
}

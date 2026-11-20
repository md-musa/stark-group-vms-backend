package com.musa.stark_group_vms.integration;

import com.musa.stark_group_vms.dto.PageResponse;
import com.musa.stark_group_vms.dto.VehicleResponse;
import com.musa.stark_group_vms.entity.Vehicle;
import com.musa.stark_group_vms.enums.FuelType;
import com.musa.stark_group_vms.enums.VehicleStatus;
import com.musa.stark_group_vms.enums.VehicleType;
import com.musa.stark_group_vms.exception.InvalidOperationException;
import com.musa.stark_group_vms.repository.VehicleRepository;
import com.musa.stark_group_vms.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class VehicleListIntegrationTest {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleRepository vehicleRepository;

    private Vehicle vehicle1;
    private Vehicle vehicle2;
    private Vehicle vehicle3;

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
                .status(VehicleStatus.IN_USE)
                .build());

        vehicle3 = vehicleRepository.save(Vehicle.builder()
                .vehicleNumber("dhaka-metro-ba-33-3003")
                .vehicleType(VehicleType.CAR)
                .brand("Honda")
                .model("Civic")
                .manufacturingYear(2022)
                .color("Red")
                .fuelType(FuelType.HYBRID)
                .seatingCapacity(5)
                .driverName("Jashim Ahmed")
                .driverPhone("01933333333")
                .status(VehicleStatus.MAINTENANCE)
                .build());
    }

    @Test
    void testRetrieveAllVehicles() {
        PageResponse<VehicleResponse> response = vehicleService.getVehicles(
                null, null, null, null, 0, 10, "createdAt", "DESC"
        );

        assertThat(response.content()).hasSize(3);
        assertThat(response.totalElements()).isEqualTo(3);
        assertThat(response.totalPages()).isEqualTo(1);
    }

    @Test
    void testEmptyDatabase() {
        vehicleRepository.deleteAll();

        PageResponse<VehicleResponse> response = vehicleService.getVehicles(
                null, null, null, null, 0, 10, "createdAt", "DESC"
        );

        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0);
        assertThat(response.totalPages()).isEqualTo(0);
        assertThat(response.page()).isEqualTo(0);
    }

    @Test
    void testPaginationAtDatabaseLevel() {
        // Page 0, Size 2
        PageResponse<VehicleResponse> page0 = vehicleService.getVehicles(
                null, null, null, null, 0, 2, "manufacturingYear", "ASC"
        );
        assertThat(page0.content()).hasSize(2);
        assertThat(page0.totalElements()).isEqualTo(3);
        assertThat(page0.totalPages()).isEqualTo(2);
        assertThat(page0.first()).isTrue();
        assertThat(page0.last()).isFalse();
        assertThat(page0.hasNext()).isTrue();
        assertThat(page0.hasPrevious()).isFalse();
        assertThat(page0.content().get(0).manufacturingYear()).isEqualTo(2018);
        assertThat(page0.content().get(1).manufacturingYear()).isEqualTo(2020);

        // Page 1, Size 2
        PageResponse<VehicleResponse> page1 = vehicleService.getVehicles(
                null, null, null, null, 1, 2, "manufacturingYear", "ASC"
        );
        assertThat(page1.content()).hasSize(1);
        assertThat(page1.totalElements()).isEqualTo(3);
        assertThat(page1.totalPages()).isEqualTo(2);
        assertThat(page1.first()).isFalse();
        assertThat(page1.last()).isTrue();
        assertThat(page1.hasNext()).isFalse();
        assertThat(page1.hasPrevious()).isTrue();
        assertThat(page1.content().get(0).manufacturingYear()).isEqualTo(2022);
    }

    @Test
    void testAscendingAndDescendingSorting() {
        // Ascending by brand
        PageResponse<VehicleResponse> asc = vehicleService.getVehicles(
                null, null, null, null, 0, 10, "brand", "ASC"
        );
        List<String> ascBrands = asc.content().stream().map(VehicleResponse::brand).toList();
        assertThat(ascBrands).containsExactly("Honda", "Mitsubishi", "Toyota");

        // Descending by brand
        PageResponse<VehicleResponse> desc = vehicleService.getVehicles(
                null, null, null, null, 0, 10, "brand", "DESC"
        );
        List<String> descBrands = desc.content().stream().map(VehicleResponse::brand).toList();
        assertThat(descBrands).containsExactly("Toyota", "Mitsubishi", "Honda");
    }

    @Test
    void testSearchByVehicleNumber() {
        // Case-insensitive & partial match
        PageResponse<VehicleResponse> result = vehicleService.getVehicles(
                "GA-11", null, null, null, 0, 10, "createdAt", "DESC"
        );
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().vehicleNumber()).isEqualTo("dhaka-metro-ga-11-1001");
    }

    @Test
    void testSearchByBrandModelAndDriverName() {
        // Search by Brand
        PageResponse<VehicleResponse> brandResult = vehicleService.getVehicles(
                "toyota", null, null, null, 0, 10, "createdAt", "DESC"
        );
        assertThat(brandResult.content()).hasSize(1);
        assertThat(brandResult.content().getFirst().brand()).isEqualTo("Toyota");

        // Search by Model
        PageResponse<VehicleResponse> modelResult = vehicleService.getVehicles(
                "pajero", null, null, null, 0, 10, "createdAt", "DESC"
        );
        assertThat(modelResult.content()).hasSize(1);
        assertThat(modelResult.content().getFirst().model()).isEqualTo("Pajero");

        // Search by Driver Name
        PageResponse<VehicleResponse> driverResult = vehicleService.getVehicles(
                "jashim", null, null, null, 0, 10, "createdAt", "DESC"
        );
        assertThat(driverResult.content()).hasSize(1);
        assertThat(driverResult.content().getFirst().driverName()).isEqualTo("Jashim Ahmed");
    }

    @Test
    void testEnumFilters() {
        // VehicleType filter
        PageResponse<VehicleResponse> typeResult = vehicleService.getVehicles(
                null, VehicleType.SUV, null, null, 0, 10, "createdAt", "DESC"
        );
        assertThat(typeResult.content()).hasSize(1);
        assertThat(typeResult.content().getFirst().vehicleType()).isEqualTo(VehicleType.SUV);

        // FuelType filter
        PageResponse<VehicleResponse> fuelResult = vehicleService.getVehicles(
                null, null, FuelType.HYBRID, null, 0, 10, "createdAt", "DESC"
        );
        assertThat(fuelResult.content()).hasSize(1);
        assertThat(fuelResult.content().getFirst().fuelType()).isEqualTo(FuelType.HYBRID);

        // VehicleStatus filter
        PageResponse<VehicleResponse> statusResult = vehicleService.getVehicles(
                null, null, null, VehicleStatus.AVAILABLE, 0, 10, "createdAt", "DESC"
        );
        assertThat(statusResult.content()).hasSize(1);
        assertThat(statusResult.content().getFirst().status()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    @Test
    void testMultipleCombinedFilters() {
        // Matching all filters
        PageResponse<VehicleResponse> matched = vehicleService.getVehicles(
                "Toyota", VehicleType.MICROBUS, FuelType.OCTANE, VehicleStatus.AVAILABLE,
                0, 10, "createdAt", "DESC"
        );
        assertThat(matched.content()).hasSize(1);
        assertThat(matched.content().getFirst().vehicleNumber()).isEqualTo("dhaka-metro-ga-11-1001");

        // Mismatched filter: brand is Toyota, but status is IN_USE
        PageResponse<VehicleResponse> mismatched = vehicleService.getVehicles(
                "Toyota", VehicleType.MICROBUS, FuelType.OCTANE, VehicleStatus.IN_USE,
                0, 10, "createdAt", "DESC"
        );
        assertThat(mismatched.content()).isEmpty();
    }

    @Test
    void testInvalidPageAndSizeValues() {
        assertThatThrownBy(() -> vehicleService.getVehicles(
                null, null, null, null, -1, 10, "createdAt", "DESC"
        ))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Page number must not be less than zero.");

        assertThatThrownBy(() -> vehicleService.getVehicles(
                null, null, null, null, 0, 0, "createdAt", "DESC"
        ))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Page size must be at least 1.");

        assertThatThrownBy(() -> vehicleService.getVehicles(
                null, null, null, null, 0, 101, "createdAt", "DESC"
        ))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Page size must not exceed 100.");
    }
}

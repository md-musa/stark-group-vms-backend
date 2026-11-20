package com.musa.stark_group_vms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musa.stark_group_vms.dto.CreateVehicleRequest;
import com.musa.stark_group_vms.dto.PageResponse;
import com.musa.stark_group_vms.dto.UpdateVehicleRequest;
import com.musa.stark_group_vms.dto.VehicleResponse;
import com.musa.stark_group_vms.enums.FuelType;
import com.musa.stark_group_vms.enums.VehicleStatus;
import com.musa.stark_group_vms.enums.VehicleType;
import com.musa.stark_group_vms.exception.DuplicateResourceException;
import com.musa.stark_group_vms.exception.InvalidOperationException;
import com.musa.stark_group_vms.exception.ResourceNotFoundException;
import com.musa.stark_group_vms.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleController.class)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private VehicleService vehicleService;

    @Test
    void createVehicle_ValidRequest_Returns201() throws Exception {
        CreateVehicleRequest request = new CreateVehicleRequest(
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

        VehicleResponse response = new VehicleResponse(
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
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(vehicleService.createVehicle(any(CreateVehicleRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.vehicleNumber").value("dhaka-metro-ga-11-2345"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void createVehicle_MissingRequiredFields_Returns400() throws Exception {
        String invalidJson = """
                {
                  "brand": "Toyota",
                  "model": "Hiace",
                  "fuelType": "OCTANE"
                }
                """;

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createVehicle_DuplicateNumber_Returns409() throws Exception {
        CreateVehicleRequest request = new CreateVehicleRequest(
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

        when(vehicleService.createVehicle(any(CreateVehicleRequest.class)))
                .thenThrow(new DuplicateResourceException(
                        "A vehicle with this vehicle number already exists."));

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("A vehicle with this vehicle number already exists."));
    }

    @Test
    void getVehicles_DefaultParameters_Returns200WithPageResponse() throws Exception {
        VehicleResponse vehicle = new VehicleResponse(
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
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        PageResponse<VehicleResponse> pageResponse = new PageResponse<>(
                List.of(vehicle),
                0,
                10,
                1L,
                1,
                true,
                true,
                false,
                false
        );

        when(vehicleService.getVehicles(null, null, null, null, 0, 10, "createdAt", "DESC"))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].vehicleNumber").value("dhaka-metro-ga-11-2345"))
                .andExpect(jsonPath("$.content[0].brand").value("Toyota"))
                .andExpect(jsonPath("$.content[0].vehicleType").value("MICROBUS"))
                .andExpect(jsonPath("$.content[0].fuelType").value("OCTANE"))
                .andExpect(jsonPath("$.content[0].status").value("AVAILABLE"));
    }

    @Test
    void getVehicles_WithSearchAndFilters_Returns200() throws Exception {
        VehicleResponse vehicle = new VehicleResponse(
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
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        PageResponse<VehicleResponse> pageResponse = new PageResponse<>(
                List.of(vehicle),
                0,
                10,
                1L,
                1,
                true,
                true,
                false,
                false
        );

        when(vehicleService.getVehicles(eq("Toyota"), eq(VehicleType.MICROBUS), eq(FuelType.OCTANE),
                eq(VehicleStatus.AVAILABLE), eq(0), eq(10), eq("brand"), eq("ASC")))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/vehicles")
                        .param("search", "Toyota")
                        .param("vehicleType", "MICROBUS")
                        .param("fuelType", "OCTANE")
                        .param("status", "AVAILABLE")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "brand")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].brand").value("Toyota"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getVehicles_EmptyDatabase_Returns200WithEmptyList() throws Exception {
        PageResponse<VehicleResponse> emptyResponse = new PageResponse<>(
                Collections.emptyList(),
                0,
                10,
                0L,
                0,
                true,
                true,
                false,
                false
        );

        when(vehicleService.getVehicles(null, null, null, null, 0, 10, "createdAt", "DESC"))
                .thenReturn(emptyResponse);

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void getVehicles_NegativePage_Returns400() throws Exception {
        when(vehicleService.getVehicles(null, null, null, null, -1, 10, "createdAt", "DESC"))
                .thenThrow(new InvalidOperationException("Page number must not be less than zero."));

        mockMvc.perform(get("/api/vehicles").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Page number must not be less than zero."));
    }

    @Test
    void getVehicles_InvalidSizeZero_Returns400() throws Exception {
        when(vehicleService.getVehicles(null, null, null, null, 0, 0, "createdAt", "DESC"))
                .thenThrow(new InvalidOperationException("Page size must be at least 1."));

        mockMvc.perform(get("/api/vehicles").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Page size must be at least 1."));
    }

    @Test
    void getVehicles_InvalidSizeGreaterThan100_Returns400() throws Exception {
        when(vehicleService.getVehicles(null, null, null, null, 0, 101, "createdAt", "DESC"))
                .thenThrow(new InvalidOperationException("Page size must not exceed 100."));

        mockMvc.perform(get("/api/vehicles").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Page size must not exceed 100."));
    }

    @Test
    void getVehicles_InvalidSortByField_Returns400() throws Exception {
        when(vehicleService.getVehicles(null, null, null, null, 0, 10, "invalidField", "DESC"))
                .thenThrow(new InvalidOperationException("Invalid sortBy field: 'invalidField'. Allowed fields are: id, vehicleNumber, vehicleType, brand, model, manufacturingYear, color, fuelType, seatingCapacity, driverName, driverPhone, status, createdAt, updatedAt."));

        mockMvc.perform(get("/api/vehicles").param("sortBy", "invalidField"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid sortBy field: 'invalidField'. Allowed fields are: id, vehicleNumber, vehicleType, brand, model, manufacturingYear, color, fuelType, seatingCapacity, driverName, driverPhone, status, createdAt, updatedAt."));
    }

    @Test
    void getVehicles_InvalidSortDirection_Returns400() throws Exception {
        when(vehicleService.getVehicles(null, null, null, null, 0, 10, "createdAt", "SIDEWAYS"))
                .thenThrow(new InvalidOperationException("Invalid sort direction: 'SIDEWAYS'. Allowed values are: ASC, DESC."));

        mockMvc.perform(get("/api/vehicles").param("direction", "SIDEWAYS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid sort direction: 'SIDEWAYS'. Allowed values are: ASC, DESC."));
    }

    @Test
    void getVehicles_InvalidEnumVehicleType_Returns400() throws Exception {
        mockMvc.perform(get("/api/vehicles").param("vehicleType", "AIRPLANE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'vehicleType': AIRPLANE"));
    }

    @Test
    void getVehicles_InvalidEnumFuelType_Returns400() throws Exception {
        mockMvc.perform(get("/api/vehicles").param("fuelType", "WATER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'fuelType': WATER"));
    }

    @Test
    void getVehicles_InvalidEnumStatus_Returns400() throws Exception {
        mockMvc.perform(get("/api/vehicles").param("status", "DESTROYED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'status': DESTROYED"));
    }

    @Test
    void getVehicles_InvalidPageFormat_Returns400() throws Exception {
        mockMvc.perform(get("/api/vehicles").param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'page': abc"));
    }

    @Test
    void updateVehicle_ValidRequest_Returns200() throws Exception {
        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "dhaka-metro-ga-11-2345",
                VehicleType.MICROBUS,
                "Toyota",
                "Hiace",
                2021,
                "Silver",
                FuelType.OCTANE,
                12,
                "Karim Uddin",
                "01712345678"
        );

        VehicleResponse response = new VehicleResponse(
                1L,
                "dhaka-metro-ga-11-2345",
                VehicleType.MICROBUS,
                "Toyota",
                "Hiace",
                2021,
                "Silver",
                FuelType.OCTANE,
                12,
                "Karim Uddin",
                "01712345678",
                VehicleStatus.AVAILABLE,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        when(vehicleService.updateVehicle(eq(1L), any(UpdateVehicleRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.vehicleNumber").value("dhaka-metro-ga-11-2345"))
                .andExpect(jsonPath("$.color").value("Silver"))
                .andExpect(jsonPath("$.manufacturingYear").value(2021))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void updateVehicle_NotFound_Returns404() throws Exception {
        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "dhaka-metro-ga-11-2345",
                VehicleType.MICROBUS,
                "Toyota",
                "Hiace",
                2021,
                "Silver",
                FuelType.OCTANE,
                12,
                "Karim Uddin",
                "01712345678"
        );

        when(vehicleService.updateVehicle(eq(999L), any(UpdateVehicleRequest.class)))
                .thenThrow(new ResourceNotFoundException("Vehicle not found with id: 999"));

        mockMvc.perform(put("/api/vehicles/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Vehicle not found with id: 999"));
    }

    @Test
    void updateVehicle_DuplicateVehicleNumber_Returns409() throws Exception {
        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "dhaka-metro-ga-11-9999",
                VehicleType.MICROBUS,
                "Toyota",
                "Hiace",
                2021,
                "Silver",
                FuelType.OCTANE,
                12,
                "Karim Uddin",
                "01712345678"
        );

        when(vehicleService.updateVehicle(eq(1L), any(UpdateVehicleRequest.class)))
                .thenThrow(new DuplicateResourceException("A vehicle with this vehicle number already exists."));

        mockMvc.perform(put("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("A vehicle with this vehicle number already exists."));
    }

    @Test
    void updateVehicle_MissingRequiredFields_Returns400() throws Exception {
        String invalidJson = """
                {
                  "color": "Silver",
                  "manufacturingYear": 2021
                }
                """;

        mockMvc.perform(put("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateVehicle_InvalidManufacturingYear_Returns400() throws Exception {
        String invalidJson = """
                {
                  "vehicleNumber": "dhaka-metro-ga-11-2345",
                  "vehicleType": "MICROBUS",
                  "brand": "Toyota",
                  "model": "Hiace",
                  "manufacturingYear": 1800,
                  "fuelType": "OCTANE",
                  "seatingCapacity": 12
                }
                """;

        mockMvc.perform(put("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.manufacturingYear").exists());
    }

    @Test
    void updateVehicle_InvalidSeatingCapacity_Returns400() throws Exception {
        String invalidJson = """
                {
                  "vehicleNumber": "dhaka-metro-ga-11-2345",
                  "vehicleType": "MICROBUS",
                  "brand": "Toyota",
                  "model": "Hiace",
                  "manufacturingYear": 2020,
                  "fuelType": "OCTANE",
                  "seatingCapacity": 0
                }
                """;

        mockMvc.perform(put("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.seatingCapacity").exists());
    }

    @Test
    void updateVehicle_InvalidEnum_Returns400() throws Exception {
        String invalidJson = """
                {
                  "vehicleNumber": "dhaka-metro-ga-11-2345",
                  "vehicleType": "SPACESHIP",
                  "brand": "Toyota",
                  "model": "Hiace",
                  "fuelType": "OCTANE"
                }
                """;

        mockMvc.perform(put("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateVehicle_InvalidIdType_Returns400() throws Exception {
        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "dhaka-metro-ga-11-2345",
                VehicleType.MICROBUS,
                "Toyota",
                "Hiace",
                2021,
                "Silver",
                FuelType.OCTANE,
                12,
                "Karim Uddin",
                "01712345678"
        );

        mockMvc.perform(put("/api/vehicles/invalidId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id': invalidId"));
    }

    @Test
    void deleteVehicle_ExistingId_Returns204() throws Exception {
        doNothing().when(vehicleService).deleteVehicle(1L);

        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteVehicle_NonExistentId_Returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Vehicle not found with id: 999"))
                .when(vehicleService).deleteVehicle(999L);

        mockMvc.perform(delete("/api/vehicles/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Vehicle not found with id: 999"));
    }

    @Test
    void deleteVehicle_InvalidIdType_Returns400() throws Exception {
        mockMvc.perform(delete("/api/vehicles/notAnId"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id': notAnId"));
    }
}

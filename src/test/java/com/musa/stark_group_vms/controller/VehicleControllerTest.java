package com.musa.stark_group_vms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musa.stark_group_vms.dto.CreateVehicleRequest;
import com.musa.stark_group_vms.dto.VehicleResponse;
import com.musa.stark_group_vms.enums.FuelType;
import com.musa.stark_group_vms.enums.VehicleStatus;
import com.musa.stark_group_vms.enums.VehicleType;
import com.musa.stark_group_vms.exception.DuplicateResourceException;
import com.musa.stark_group_vms.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}

package com.musa.stark_group_vms.dto;

import com.musa.stark_group_vms.enums.FuelType;
import com.musa.stark_group_vms.enums.VehicleType;
import jakarta.validation.constraints.*;

public record UpdateVehicleRequest(

        @NotBlank(message = "Vehicle number must not be blank")
        @Size(max = 50, message = "Vehicle number must not exceed 50 characters")
        String vehicleNumber,

        @NotNull(message = "Vehicle type must not be null")
        VehicleType vehicleType,

        @NotBlank(message = "Brand must not be blank")
        @Size(max = 100, message = "Brand must not exceed 100 characters")
        String brand,

        @NotBlank(message = "Model must not be blank")
        @Size(max = 100, message = "Model must not exceed 100 characters")
        String model,

        @Min(value = 1900, message = "Manufacturing year must be 1900 or later")
        @Max(value = 2100, message = "Manufacturing year must not be in the far future")
        Integer manufacturingYear,

        @Size(max = 50, message = "Color must not exceed 50 characters")
        String color,

        @NotNull(message = "Fuel type must not be null")
        FuelType fuelType,

        @Positive(message = "Seating capacity must be a positive number")
        Integer seatingCapacity,

        @Size(max = 100, message = "Driver name must not exceed 100 characters")
        String driverName,

        @Size(max = 20, message = "Driver phone must not exceed 20 characters")
        @Pattern(regexp = "^[0-9+\\-() ]*$", message = "Driver phone must contain only valid phone number characters")
        String driverPhone
) {
}

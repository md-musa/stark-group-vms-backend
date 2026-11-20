package com.musa.stark_group_vms.dto;

import com.musa.stark_group_vms.enums.VehicleStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateVehicleStatusRequest(
        @NotNull(message = "Status must not be null")
        VehicleStatus status
) {
}

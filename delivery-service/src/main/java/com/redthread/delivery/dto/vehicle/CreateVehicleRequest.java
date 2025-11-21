package com.redthread.delivery.dto.vehicle;

import jakarta.validation.constraints.NotBlank;

public record CreateVehicleRequest(
        @NotBlank String plate,
        String model,
        String capacityKg,
        Boolean active
) { }

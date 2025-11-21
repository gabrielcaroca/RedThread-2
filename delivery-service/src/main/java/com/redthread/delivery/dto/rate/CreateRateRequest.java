package com.redthread.delivery.dto.rate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateRateRequest(
        @NotNull Long zoneId,
        @NotNull @Min(0) Long basePrice,
        Boolean isActive
) { }

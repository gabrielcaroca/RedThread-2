package com.redthread.delivery.dto.shipment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignRequest(
        @NotNull @Positive Long assignedUserId
) { }

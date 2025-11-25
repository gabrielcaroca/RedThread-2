package com.redthread.delivery.dto.shipment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(name="AssignRequest", description = "Asignación directa de un envío a un repartidor.")
public record AssignRequest(
        @Schema(example = "55", description = "ID del usuario repartidor/driver")
        @NotNull @Positive Long assignedUserId
) { }

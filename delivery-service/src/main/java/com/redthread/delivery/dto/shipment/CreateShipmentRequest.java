package com.redthread.delivery.dto.shipment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(name = "CreateShipmentRequest", description = "Crea un envío desde una orden existente.")
public record CreateShipmentRequest(
        @Schema(example = "101", description = "ID de la orden en orders-service")
        @NotNull @Positive Long orderId
) { }

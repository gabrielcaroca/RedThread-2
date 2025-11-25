package com.redthread.delivery.dto.shipment;

import com.redthread.delivery.domain.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="TrackRequest", description="Registra un evento de tracking manual.")
public record TrackRequest(
        @Schema(example = "IN_TRANSIT")
        DeliveryStatus status,

        @Schema(example = "-33.4489")
        String latitude,

        @Schema(example = "-70.6693")
        String longitude,

        @Schema(example = "Cliente no estaba, reintento mañana")
        String note
) { }

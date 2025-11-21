package com.redthread.delivery.dto.shipment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateShipmentRequest(@NotNull @Positive Long orderId) { }

package com.redthread.delivery.dto.shipment;

import com.redthread.delivery.domain.DeliveryStatus;

import java.time.Instant;

public record ShipmentResponse(
        Long id,
        Long orderId,
        Long userId,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String zip,
        String country,
        Long zoneId,
        DeliveryStatus status,
        String totalPrice,
        Instant createdAt,
        Instant updatedAt
) { }

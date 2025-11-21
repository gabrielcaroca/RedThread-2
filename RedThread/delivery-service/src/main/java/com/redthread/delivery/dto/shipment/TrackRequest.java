package com.redthread.delivery.dto.shipment;

import com.redthread.delivery.domain.DeliveryStatus;

public record TrackRequest(
        DeliveryStatus status,
        String latitude,
        String longitude,
        String note
) { }

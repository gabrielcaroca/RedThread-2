package com.redthread.delivery.dto.shipment;

import com.redthread.delivery.domain.DeliveryStatus;
import com.redthread.delivery.domain.Shipment;

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

        DeliveryStatus status,
        Long assignedUserId,
        String totalPrice,

        String evidenceUrl,
        String receiverName,
        String note,

        Instant createdAt,
        Instant updatedAt
) {
    public static ShipmentResponse from(Shipment s) {
        return new ShipmentResponse(
                s.getId(), s.getOrderId(), s.getUserId(),
                s.getAddressLine1(), s.getAddressLine2(),
                s.getCity(), s.getState(), s.getZip(), s.getCountry(),
                s.getStatus(), s.getAssignedUserId(),
                s.getTotalPrice() == null ? "0" : s.getTotalPrice().toPlainString(),
                s.getEvidenceUrl(), s.getReceiverName(), s.getNote(),
                s.getCreatedAt(), s.getUpdatedAt()
        );
    }
}

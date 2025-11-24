package com.redthread.delivery.integration.dto;

public record OrderDeliveryResponse(
        Long userId,
        ShippingAddress shippingAddress
) {}

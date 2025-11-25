package com.redthread.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(name = "OrderDeliveryRes", description = "DTO interno para delivery-service")
public record OrderDeliveryRes(
    @Schema(description = "ID de orden", example = "12")
    Long id,
    @Schema(description = "Estado actual", example = "CREATED")
    String status,
    @Schema(description = "Total", example = "29980.00")
    BigDecimal totalAmount,
    @Schema(description = "UserId dueño", example = "u123")
    String userId,
    @Schema(description = "Dirección de envío")
    AddressRes shippingAddress,
    @Schema(description = "Items")
    List<OrderItemRes> items
) {}

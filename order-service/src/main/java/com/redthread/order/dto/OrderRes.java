package com.redthread.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(name = "OrderRes", description = "Orden del usuario")
public record OrderRes(
    @Schema(description = "ID de orden", example = "12")
    Long id,
    @Schema(description = "Estado actual", example = "CREATED")
    String status,
    @Schema(description = "Total", example = "29980.00")
    BigDecimal totalAmount,
    @Schema(description = "Items")
    List<OrderItemRes> items
) {}

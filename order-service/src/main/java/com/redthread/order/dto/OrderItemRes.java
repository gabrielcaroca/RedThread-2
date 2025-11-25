package com.redthread.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "OrderItemRes", description = "Item de orden")
public record OrderItemRes(
    @Schema(description = "VariantId del catálogo", example = "10")
    Long variantId,
    @Schema(description = "Cantidad", example = "2")
    Integer quantity,
    @Schema(description = "Precio unitario", example = "14990.00")
    BigDecimal unitPrice,
    @Schema(description = "Subtotal item", example = "29980.00")
    BigDecimal lineTotal
) {}

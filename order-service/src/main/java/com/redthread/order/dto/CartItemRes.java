package com.redthread.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "CartItemRes", description = "Item del carrito")
public record CartItemRes(
    @Schema(description = "ID interno del item", example = "5")
    Long itemId,
    @Schema(description = "VariantId del catálogo", example = "10")
    Long variantId,
    @Schema(description = "Cantidad", example = "2")
    Integer quantity,
    @Schema(description = "Precio unitario", example = "14990.00")
    BigDecimal unitPrice
) {}

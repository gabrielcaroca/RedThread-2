package com.redthread.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(name = "CartRes", description = "Carrito completo")
public record CartRes(
    @Schema(description = "ID del carrito", example = "1")
    Long cartId,
    @Schema(description = "Items")
    List<CartItemRes> items,
    @Schema(description = "Total calculado", example = "29980.00")
    BigDecimal total
) {}

package com.redthread.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(name = "AddItemReq", description = "Request para agregar item al carrito")
public record AddItemReq(
    @Schema(description = "ID de variante del catálogo", example = "10")
    @NotNull Long variantId,
    @Schema(description = "Cantidad a agregar", example = "2", minimum = "1")
    @NotNull @Min(1) Integer quantity
) {}

package com.redthread.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UpdateItemReq", description = "Alias legacy para actualizar item {itemId, quantity}")
public record UpdateItemReq(
    @Schema(description = "ID interno del item", example = "5")
    @NotNull Long itemId,
    @Schema(description = "Nueva cantidad", example = "2", minimum = "1")
    @NotNull @Min(1) Integer quantity
) {}

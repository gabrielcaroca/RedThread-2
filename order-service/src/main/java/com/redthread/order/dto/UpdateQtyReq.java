package com.redthread.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UpdateQtyReq", description = "Request para actualizar cantidad")
public record UpdateQtyReq(
    @Schema(description = "Nueva cantidad", example = "3", minimum = "1")
    @NotNull @Min(1) Integer quantity
) {}

package com.redthread.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "CheckoutReq", description = "Request de checkout")
public record CheckoutReq(
    @Schema(description = "ID de dirección elegida", example = "3")
    @NotNull Long addressId
) {}

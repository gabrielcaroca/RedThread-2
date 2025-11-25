package com.redthread.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PayReq", description = "Request opcional para pagar orden")
public record PayReq(
    @Schema(description = "Proveedor (si aplica)", example = "FAKEPAY")
    String provider
) {}

package com.redthread.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "AddressReq", description = "Request para crear/editar dirección")
public record AddressReq(
    @Schema(description = "Linea 1", example = "Av. Siempre Viva 123")
    @NotNull String line1,
    @Schema(description = "Linea 2", example = "Depto 45")
    String line2,
    @Schema(description = "Ciudad", example = "Santiago")
    @NotNull String city,
    @Schema(description = "Región/Estado", example = "RM")
    @NotNull String state,
    @Schema(description = "Código postal", example = "8320000")
    @NotNull String zip,
    @Schema(description = "País", example = "Chile")
    @NotNull String country,
    @Schema(description = "Si es dirección por defecto", example = "true")
    Boolean isDefault
) {}

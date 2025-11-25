package com.redthread.catalog.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "AdjustStockReq", description = "Ajuste de stock disponible para una variante.")
public record AdjustStockReq(
        @Schema(example = "5", description = "ID de variante")
        @NotNull Long variantId,

        @Schema(example = "-2", description = "Delta a aplicar al stockAvailable (puede ser negativo)")
        @NotNull Integer delta
) {}

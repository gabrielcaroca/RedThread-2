package com.redthread.catalog.controller.dto;

import com.redthread.catalog.model.enums.SizeType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;


public record CreateVariantReq(
        @Schema(example = "10", description = "ID del producto padre")
        @NotNull Long productId,

        @Schema(example = "LETTER", description = "Tipo de talla: EU o LETTER")
        @NotNull SizeType sizeType,

        @Schema(example = "M", description = "Valor de la talla")
        @NotBlank String sizeValue,

        @Schema(example = "NEGRO", description = "Color de la variante")
        @NotBlank String color,
        
        @Schema(example = "SKU-10-M-NEGRO", description = "SKU opcional")
        String sku,

        @Schema(example = "5", description = "Stock inicial (opcional)")
        @Min(0) Integer stock
) {}

package com.redthread.catalog.controller.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "VariantDto", description = "Representación de una variante de producto")
public record VariantDto(
        @Schema(example = "15")
        Long id,
        @Schema(example = "10")
        Long productId,

        @Schema(example = "LETTER")
        String sizeType,

        @Schema(example = "M")
        String sizeValue,

        @Schema(example = "NEGRO")
        String color,

        @Schema(example = "SKU-10-M-NEGRO")
        String sku,

        @Schema(example = "14990.00")
        BigDecimal priceOverride,
        
        @Schema(example = "5")
        Integer stock
) {}

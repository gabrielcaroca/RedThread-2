package com.redthread.catalog.controller.dto;

import java.math.BigDecimal;

public record VariantDto(
        Long id,
        Long productId,
        String sizeType,
        String sizeValue,
        String color,
        String sku,
        BigDecimal priceOverride,
        Integer stock
) {}

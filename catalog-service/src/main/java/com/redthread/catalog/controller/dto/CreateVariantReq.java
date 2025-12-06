package com.redthread.catalog.controller.dto;

import com.redthread.catalog.model.enums.SizeType;
import jakarta.validation.constraints.*;


public record CreateVariantReq(
        @NotNull Long productId,
        @NotNull SizeType sizeType,
        @NotBlank String sizeValue,
        @NotBlank String color,
        String sku,
        @Min(0) Integer stock
) {}

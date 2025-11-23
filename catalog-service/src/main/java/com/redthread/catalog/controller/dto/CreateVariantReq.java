package com.redthread.catalog.controller.dto;

import com.redthread.catalog.model.enums.SizeType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateVariantReq(
        @NotNull Long productId,
        @NotNull SizeType sizeType,
        @NotBlank String sizeValue,
        @NotBlank String color,
        String sku,
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal priceOverride,
        @Min(0) Integer stock   
) {}

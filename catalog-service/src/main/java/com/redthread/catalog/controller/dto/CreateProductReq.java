package com.redthread.catalog.controller.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductReq(
        @NotNull Long categoryId,
        Long brandId,
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin(value="0.0", inclusive = true) BigDecimal basePrice
) {}

package com.redthread.catalog.controller.dto;

import jakarta.validation.constraints.*;

public record AdjustStockReq(
        @NotNull Long variantId,
        @NotNull Integer delta
) {}


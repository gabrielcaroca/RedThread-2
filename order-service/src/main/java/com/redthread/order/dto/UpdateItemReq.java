package com.redthread.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateItemReq(
    @NotNull Long itemId,
    @NotNull @Min(1) Integer quantity
) {}

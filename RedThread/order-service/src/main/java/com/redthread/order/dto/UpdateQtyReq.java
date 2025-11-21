package com.redthread.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateQtyReq(@NotNull @Min(1) Integer quantity) {}
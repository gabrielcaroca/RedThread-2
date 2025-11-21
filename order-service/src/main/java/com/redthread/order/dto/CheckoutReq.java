package com.redthread.order.dto;

import jakarta.validation.constraints.NotNull;

public record CheckoutReq(@NotNull Long addressId) {}

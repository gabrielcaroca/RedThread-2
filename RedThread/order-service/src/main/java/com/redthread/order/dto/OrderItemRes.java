package com.redthread.order.dto;

import java.math.BigDecimal;

public record OrderItemRes(Long variantId, Integer quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}

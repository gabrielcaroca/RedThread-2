package com.redthread.order.dto;

import java.math.BigDecimal;

public record CartItemRes(Long itemId, Long variantId, Integer quantity, BigDecimal unitPrice) {}
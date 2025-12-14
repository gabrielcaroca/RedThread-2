package com.redthread.order.dto;

import java.math.BigDecimal;

public record AdminOrderItemRes(
    Long variantId,
    String productName,
    String size,
    String color,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal
) {}

package com.redthread.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminOrderDetailRes(
    Long id,
    String status,
    String userId,
    String fullAddress,
    BigDecimal totalAmount,
    List<AdminOrderItemRes> items
) {}

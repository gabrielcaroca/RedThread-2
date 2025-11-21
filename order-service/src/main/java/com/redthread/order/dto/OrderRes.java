package com.redthread.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderRes(Long id, String status, BigDecimal totalAmount, List<OrderItemRes> items) {}

package com.redthread.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartRes(Long cartId, List<CartItemRes> items, BigDecimal total) {}

package com.redthread.order.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO interno SOLO para delivery-service.
 * Delivery necesita:
 *  - userId para validar dueño del pedido
 *  - shippingAddress con la estructura esperada por ShipmentServiceImpl
 */
public record OrderDeliveryRes(
    Long id,
    String status,
    BigDecimal totalAmount,
    String userId,
    AddressRes shippingAddress,
    List<OrderItemRes> items
) {}

package com.redthread.order.service;

import com.redthread.order.dto.CheckoutReq;
import com.redthread.order.integrations.CatalogClient;
import com.redthread.order.model.*;
import com.redthread.order.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutService {

  private final CartRepository cartRepo;
  private final CartItemRepository itemRepo;
  private final AddressRepository addressRepo;
  private final OrderRepository orderRepo;
  private final OrderItemRepository orderItemRepo;
  private final PaymentAttemptRepository payRepo;
  private final CatalogClient catalog;

  @Transactional
  public Order checkout(String userId, CheckoutReq req) {
    Cart cart = cartRepo.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("Carrito no existe"));

    List<CartItem> items = itemRepo.findByCartId(cart.getId());
    if (items.isEmpty()) {
      throw new IllegalStateException("Carrito vacío");
    }

    Address address = addressRepo.findByIdAndUserId(req.addressId(), userId)
        .orElseThrow(() -> new IllegalArgumentException("Dirección inválida"));

    // Crear orden base en estado CREATED
    Order order = Order.builder()
        .userId(userId)
        .address(address)
        .status(OrderStatus.CREATED)
        .totalAmount(BigDecimal.ZERO)
        .createdAt(Instant.now())
        .build();
    order = orderRepo.save(order);

    BigDecimal total = BigDecimal.ZERO;

    for (CartItem it : items) {
      BigDecimal unit;
      try {
        var variant = catalog.findVariantById(it.getVariantId());
        unit = variant != null && variant.price() != null
            ? variant.price()
            : (it.getUnitPrice() != null ? it.getUnitPrice() : BigDecimal.ZERO);

      } catch (Exception e) {
        unit = it.getUnitPrice() != null ? it.getUnitPrice() : BigDecimal.ZERO;
      }

      BigDecimal line = unit.multiply(BigDecimal.valueOf(it.getQuantity()));
      total = total.add(line);

      // ============================
      // Descontar stock en Catalog
      // ============================
      try {
        // delta negativo -> reduce stock disponible
        catalog.adjustStock(it.getVariantId(), -it.getQuantity());
      } catch (Exception ex) {
        throw new IllegalStateException(
            "No se pudo ajustar el stock para la variante " + it.getVariantId(), ex);
      }

      orderItemRepo.save(OrderItem.builder()
          .order(order)
          .variantId(it.getVariantId())
          .quantity(it.getQuantity())
          .unitPrice(unit)
          .lineTotal(line)
          .build());
    }

    order.setTotalAmount(total);
    orderRepo.save(order);

    itemRepo.deleteByCartId(cart.getId());
    cart.setUpdatedAt(Instant.now());
    cartRepo.save(cart);

    payRepo.save(PaymentAttempt.builder()
        .order(order)
        .provider(null)
        .status(PaymentStatus.PENDING)
        .createdAt(Instant.now())
        .build());

    return order;
  }
}

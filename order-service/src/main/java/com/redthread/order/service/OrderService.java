package com.redthread.order.service;

import com.redthread.order.model.*;
import com.redthread.order.repository.OrderItemRepository;
import com.redthread.order.repository.OrderRepository;
import com.redthread.order.repository.PaymentAttemptRepository;
import com.redthread.order.integrations.CatalogClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepo;
  private final OrderItemRepository itemRepo;
  private final PaymentAttemptRepository payRepo;
  private final CatalogClient catalog;

  public List<Order> listByUser(String userId) {
    var orders = orderRepo.findByUserIdOrderByIdDesc(userId);
    orders.forEach(o -> o.setItems(itemRepo.findByOrderId(o.getId())));
    return orders;
  }

  public Order getByIdForUser(Long id, String userId) {
    var order = orderRepo.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new IllegalArgumentException("Order no encontrada"));
    order.setItems(itemRepo.findByOrderId(order.getId()));
    return order;
  }

  /**
   * Webhook interno llamado por delivery-service.
   * Enum OrderStatus: {CREATED, PAID, CANCELLED, SHIPPED}
   * Mapeo:
   * - DELIVERED -> SHIPPED
   * - FAILED    -> CANCELLED
   * - otros     -> no cambia nada
   */
  @Transactional
  public void updateDeliveryStatusInternal(Long orderId, String deliveryStatus, String note) {
    Order order = orderRepo.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order no encontrada"));

    if (deliveryStatus == null) return;

    String s = deliveryStatus.trim().toUpperCase();

    switch (s) {
      case "DELIVERED" -> order.setStatus(OrderStatus.SHIPPED);
      case "FAILED" -> order.setStatus(OrderStatus.CANCELLED);
      default -> {
        // ASSIGNED / IN_TRANSIT / PENDING_PICKUP / etc.
        // no afectan el estado de la orden
        return;
      }
    }

    // note por ahora no se guarda porque Order no tiene campo para eso.
    // Si mas adelante agregas historial/notas, se guarda aqui.

    orderRepo.save(order);
  }

  @Transactional
  public Order pay(String userId, Long orderId, String provider) {
    Order order = getByIdForUser(orderId, userId);
    if (order.getStatus() != OrderStatus.CREATED)
      throw new IllegalStateException("Solo CREATED puede pagarse");

    // registrar intento/pago (provider puede ser null)
    payRepo.save(PaymentAttempt.builder()
        .order(order)
        .provider(provider == null ? "DEFAULT" : provider)
        .status(PaymentStatus.APPROVED)
        .createdAt(java.time.Instant.now())
        .build());

    order.setStatus(OrderStatus.PAID);
    return orderRepo.save(order);
  }

  @Transactional
  public Order cancel(String userId, Long orderId) {
    Order order = getByIdForUser(orderId, userId);
    if (order.getStatus() == OrderStatus.CANCELLED) return order;
    if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PAID)
      throw new IllegalStateException("Solo CREATED o PAID se pueden cancelar");

    var items = itemRepo.findByOrderId(order.getId());
    for (var it : items) {
      catalog.adjustStock(it.getVariantId(), +it.getQuantity());
    }

    order.setStatus(OrderStatus.CANCELLED);
    return orderRepo.save(order);
  }
}

package com.redthread.order.service;

import com.redthread.order.integrations.CatalogClient;
import com.redthread.order.model.*;
import com.redthread.order.repository.OrderItemRepository;
import com.redthread.order.repository.OrderRepository;
import com.redthread.order.repository.PaymentAttemptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock OrderRepository orderRepo;
  @Mock OrderItemRepository itemRepo;
  @Mock PaymentAttemptRepository payRepo;
  @Mock CatalogClient catalog;

  @InjectMocks OrderService service;

  @Test
  void listByUser_setsItems() {
    String userId = "u1";

    Order o = Order.builder()
        .id(1L).userId(userId).status(OrderStatus.CREATED)
        .totalAmount(BigDecimal.ZERO).createdAt(Instant.now())
        .build();

    when(orderRepo.findByUserIdOrderByIdDesc(userId)).thenReturn(List.of(o));
    when(itemRepo.findByOrderId(1L)).thenReturn(List.of(
        OrderItem.builder().id(10L).order(o).variantId(5L).quantity(1)
            .unitPrice(new BigDecimal("1000.00")).lineTotal(new BigDecimal("1000.00")).build()
    ));

    var list = service.listByUser(userId);

    assertThat(list).hasSize(1);
    assertThat(list.get(0).getItems()).hasSize(1);
  }

  @Test
  void cancel_happyPath_restocksAndCancels() {
    String userId = "u1";

    Order o = Order.builder()
        .id(1L).userId(userId).status(OrderStatus.CREATED)
        .totalAmount(new BigDecimal("1000.00")).createdAt(Instant.now())
        .build();

    when(orderRepo.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(o));
    when(itemRepo.findByOrderId(1L)).thenReturn(List.of(
        OrderItem.builder().id(10L).order(o).variantId(5L).quantity(2)
            .unitPrice(new BigDecimal("500.00")).lineTotal(new BigDecimal("1000.00")).build()
    ));
    when(orderRepo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

    Order cancelled = service.cancel(userId, 1L);

    assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    verify(catalog).adjustStock(5L, +2);
  }
}

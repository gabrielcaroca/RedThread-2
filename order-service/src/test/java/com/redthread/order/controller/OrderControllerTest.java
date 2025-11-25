package com.redthread.order.controller;

import com.redthread.order.dto.OrderItemRes;
import com.redthread.order.dto.OrderRes;
import com.redthread.order.model.Order;
import com.redthread.order.model.OrderItem;
import com.redthread.order.model.OrderStatus;
import com.redthread.order.security.JwtUserResolver;
import com.redthread.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

  @Autowired MockMvc mvc;

  @MockBean OrderService orderService;
  @MockBean JwtUserResolver auth;

  @Test
  void list_returnsOrders() throws Exception {
    when(auth.currentUserId()).thenReturn("u1");

    Order o = Order.builder()
        .id(1L)
        .userId("u1")
        .status(OrderStatus.CREATED)
        .totalAmount(new BigDecimal("1000.00"))
        .createdAt(Instant.now())
        .items(List.of(
            OrderItem.builder()
                .variantId(10L).quantity(1)
                .unitPrice(new BigDecimal("1000.00"))
                .lineTotal(new BigDecimal("1000.00"))
                .build()
        ))
        .build();

    when(orderService.listByUser("u1")).thenReturn(List.of(o));

    mvc.perform(get("/orders"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].items[0].variantId").value(10));
  }

  @Test
  void cancel_whenIllegalState_returns409() throws Exception {
    when(auth.currentUserId()).thenReturn("u1");
    when(orderService.cancel("u1", 1L)).thenThrow(new IllegalStateException("Solo CREATED o PAID se pueden cancelar"));

    mvc.perform(post("/orders/1/cancel"))
        .andExpect(status().isConflict());
  }
}

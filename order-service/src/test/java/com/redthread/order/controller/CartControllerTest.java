package com.redthread.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redthread.order.dto.*;
import com.redthread.order.security.JwtUserResolver;
import com.redthread.order.service.CartService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper om;

  @MockBean CartService cartService;
  @MockBean JwtUserResolver auth;

  @Test
  void postItems_happyPath_returnsCart() throws Exception {
    when(auth.currentUserId()).thenReturn("u1");

    CartRes res = new CartRes(1L,
        List.of(new CartItemRes(5L, 10L, 2, new BigDecimal("1000.00"))),
        new BigDecimal("2000.00")
    );

    when(cartService.addItem(Mockito.eq("u1"), Mockito.any(AddItemReq.class))).thenReturn(res);

    mvc.perform(post("/cart/items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(new AddItemReq(10L, 2))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cartId").value(1))
        .andExpect(jsonPath("$.items[0].variantId").value(10))
        .andExpect(jsonPath("$.total").value(2000.00));
  }

  @Test
  void patchItems_invalidQty_returns400() throws Exception {
    when(auth.currentUserId()).thenReturn("u1");

    mvc.perform(patch("/cart/items/5")
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(new UpdateQtyReq(0))))
        .andExpect(status().isBadRequest());
  }
}

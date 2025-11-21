package com.redthread.order.controller;

import com.redthread.order.dto.*;
import com.redthread.order.security.JwtUserResolver;
import com.redthread.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;
  private final JwtUserResolver auth;

  @PostMapping
  public CartRes createOrGet() {
    return cartService.getCart(auth.currentUserId());
  }

  @GetMapping
  public CartRes detail() {
    return cartService.getCart(auth.currentUserId());
  }

  @PostMapping("/items")
  public CartRes addItem(@Valid @RequestBody AddItemReq req) {
    return cartService.addItem(auth.currentUserId(), req);
  }

  @PatchMapping("/items/{itemId}")
  public CartRes updateItem(@PathVariable Long itemId, @Valid @RequestBody UpdateQtyReq req) {
    return cartService.updateItem(auth.currentUserId(), itemId, req);
  }

  @DeleteMapping("/items/{itemId}")
  public void deleteItem(@PathVariable Long itemId) {
    cartService.removeItem(auth.currentUserId(), itemId);
  }
}

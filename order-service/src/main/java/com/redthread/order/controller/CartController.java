package com.redthread.order.controller;

import com.redthread.order.dto.AddItemReq;
import com.redthread.order.dto.CartRes;
import com.redthread.order.dto.UpdateItemReq;
import com.redthread.order.dto.UpdateQtyReq;
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

  // ====== EXISTENTES ======
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

  // POST /cart/add
  @PostMapping("/add")
  public CartRes addAlias(@Valid @RequestBody AddItemReq req) {
    return cartService.addItem(auth.currentUserId(), req);
  }

  // POST /cart/update  { itemId, quantity }
  @PostMapping("/update")
  public CartRes updateAlias(@Valid @RequestBody UpdateItemReq req) {
    return cartService.updateItem(
        auth.currentUserId(),
        req.itemId(),
        new UpdateQtyReq(req.quantity())
    );
  }

  // DELETE /cart/item/{productId}  (productId = variantId real)
  @DeleteMapping("/item/{productId}")
  public void deleteByVariant(@PathVariable("productId") Long variantId) {
    cartService.removeItemByVariant(auth.currentUserId(), variantId);
  }

  // DELETE /cart/clear
  @DeleteMapping("/clear")
  public void clear() {
    cartService.clearCart(auth.currentUserId());
  }
}

package com.redthread.order.service;

import com.redthread.order.dto.AddItemReq;
import com.redthread.order.dto.CartItemRes;
import com.redthread.order.dto.CartRes;
import com.redthread.order.dto.UpdateQtyReq;
import com.redthread.order.integrations.CatalogClient;
import com.redthread.order.model.Cart;
import com.redthread.order.model.CartItem;
import com.redthread.order.repository.CartItemRepository;
import com.redthread.order.repository.CartRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
  private final CartRepository cartRepo;
  private final CartItemRepository itemRepo;
  private final CatalogClient catalog;

  @Transactional
  public Cart requireCart(String userId) {
    return cartRepo.findByUserId(userId).orElseGet(() -> {
      Cart c = Cart.builder()
          .userId(userId)
          .createdAt(Instant.now())
          .updatedAt(Instant.now())
          .build();
      return cartRepo.save(c);
    });
  }

  @Transactional
  public CartRes getCart(String userId) {
    Cart c = requireCart(userId);
    List<CartItem> items = itemRepo.findByCartId(c.getId());
    var resItems = items.stream()
        .map(i -> new CartItemRes(i.getId(), i.getVariantId(), i.getQuantity(), i.getUnitPrice()))
        .toList();
    BigDecimal total = items.stream()
        .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return new CartRes(c.getId(), resItems, total);
  }

  @Transactional
  public CartRes addItem(String userId, AddItemReq req) {
    Cart c = requireCart(userId);

    var info = catalog.findVariantById(req.variantId());
    BigDecimal computedUnit = (info != null && info.price() != null) ? info.price() : BigDecimal.ZERO;
    if (computedUnit.compareTo(BigDecimal.ZERO) == 0) {
      throw new IllegalStateException("Precio no disponible para este variant");
    }

    final Long cartId = c.getId();
    final Long fVariantId = req.variantId();
    final Integer fQty = req.quantity();
    final BigDecimal fUnit = computedUnit;

    CartItem item = itemRepo.findByCartIdAndVariantId(cartId, fVariantId)
        .map(existing -> {
          existing.setQuantity(existing.getQuantity() + fQty);
          if (fUnit != null && fUnit.compareTo(BigDecimal.ZERO) > 0) {
            existing.setUnitPrice(fUnit);
          }
          return existing;
        })
        .orElseGet(() -> CartItem.builder()
            .cart(c)
            .variantId(fVariantId)
            .quantity(fQty)
            .unitPrice((fUnit != null) ? fUnit : BigDecimal.ZERO)
            .build());

    itemRepo.save(item);
    c.setUpdatedAt(Instant.now());
    return getCart(userId);
  }

  @Transactional
  public CartRes updateItem(String userId, Long itemId, UpdateQtyReq req) {
    Cart c = requireCart(userId);
    CartItem it = itemRepo.findByIdAndCartId(itemId, c.getId())
        .orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));
    it.setQuantity(req.quantity());
    itemRepo.save(it);
    c.setUpdatedAt(Instant.now());
    return getCart(userId);
  }

  @Transactional
  public void removeItem(String userId, Long itemId) {
    Cart c = requireCart(userId);
    CartItem it = itemRepo.findByIdAndCartId(itemId, c.getId())
        .orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));
    itemRepo.delete(it);
    c.setUpdatedAt(Instant.now());
  }
}

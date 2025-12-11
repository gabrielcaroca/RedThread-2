package com.redthread.order.service;

import com.redthread.order.dto.AddItemReq;
import com.redthread.order.dto.CartItemRes;
import com.redthread.order.dto.CartRes;
import com.redthread.order.dto.UpdateItemReq;
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

  // ==========================
  // Helpers internos
  // ==========================

  private Cart requireCart(String userId) {
    return cartRepo.findByUserId(userId)
        .orElseGet(() -> {
          Instant now = Instant.now();
          Cart c = Cart.builder()
              .userId(userId)
              .createdAt(now)
              .updatedAt(now)
              .build();
          return cartRepo.save(c);
        });
  }

  private CartRes buildResponse(Cart cart) {
    List<CartItem> items = itemRepo.findByCartId(cart.getId());

    List<CartItemRes> resItems = items.stream()
        .map(i -> new CartItemRes(
            i.getId(),
            i.getVariantId(),
            i.getQuantity(),
            i.getUnitPrice()
        ))
        .toList();

    BigDecimal total = resItems.stream()
        .map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return new CartRes(cart.getId(), resItems, total);
  }

  // ==========================
  // GET /cart
  // ==========================
  @Transactional
  public CartRes getCart(String userId) {
    Cart c = requireCart(userId);
    return buildResponse(c);
  }

  // ==========================
  // POST /cart/items
  // ==========================
  @Transactional
  public CartRes addItem(String userId, AddItemReq req) {
    Cart c = requireCart(userId);

    // Buscamos info de la variante en catalog-service para obtener el precio
    var info = catalog.findVariantById(req.variantId());
    BigDecimal computedUnit =
        (info != null && info.price() != null) ? info.price() : BigDecimal.ZERO;

    // Si por alguna razón no llega precio, no tiramos excepción:
    // dejamos el precio en 0 y el total será 0, pero el item se guarda igual.
    // (Si quieres forzar error, aquí puedes lanzar IllegalStateException).
    final Long cartId = c.getId();
    final Long variantId = req.variantId();
    final Integer deltaQty = req.quantity();

    CartItem item = itemRepo.findByCartIdAndVariantId(cartId, variantId)
        .orElseGet(() -> CartItem.builder()
            .cart(c)
            .variantId(variantId)
            .quantity(0)
            .unitPrice(computedUnit)
            .build()
        );

    int newQty = item.getQuantity() + deltaQty;
    item.setQuantity(newQty);

    // si el catálogo devuelve un precio válido, lo actualizamos
    if (computedUnit.compareTo(BigDecimal.ZERO) > 0) {
      item.setUnitPrice(computedUnit);
    }

    // 🔴 AQUÍ estaba el problema: nos aseguramos de PERSISTIR siempre el item
    itemRepo.save(item);

    c.setUpdatedAt(Instant.now());
    cartRepo.save(c);

    return buildResponse(c);
  }

  // ==========================
  // PATCH /cart/items/{itemId}
  // ==========================
  @Transactional
  public CartRes updateItem(String userId, Long itemId, UpdateQtyReq req) {
    Cart c = requireCart(userId);

    CartItem item = itemRepo.findByIdAndCartId(itemId, c.getId())
        .orElseThrow(() -> new IllegalArgumentException("Item no encontrado en el carrito"));

    item.setQuantity(req.quantity());
    itemRepo.save(item);

    c.setUpdatedAt(Instant.now());
    cartRepo.save(c);

    return buildResponse(c);
  }

  // ==========================
  // POST alias legacy (UpdateItemReq)
  // ==========================
  @Transactional
  public CartRes updateItem(String userId, UpdateItemReq req) {
    return updateItem(userId, req.itemId(), new UpdateQtyReq(req.quantity()));
  }

  // ==========================
  // DELETE /cart/items/{itemId}
  // ==========================
  @Transactional
  public void removeItem(String userId, Long itemId) {
    Cart c = requireCart(userId);
    itemRepo.findByIdAndCartId(itemId, c.getId())
        .ifPresent(itemRepo::delete);

    c.setUpdatedAt(Instant.now());
    cartRepo.save(c);
  }

  // ==========================
  // DELETE /cart/items/by-product/{productId}
  // ==========================
  @Transactional
  public void removeItemByVariant(String userId, Long variantId) {
    Cart c = requireCart(userId);
    itemRepo.deleteByCartIdAndVariantId(c.getId(), variantId);
    c.setUpdatedAt(Instant.now());
    cartRepo.save(c);
  }

  // ==========================
  // DELETE /cart/clear
  // ==========================
  @Transactional
  public void clearCart(String userId) {
    Cart c = requireCart(userId);
    itemRepo.deleteByCartId(c.getId());
    c.setUpdatedAt(Instant.now());
    cartRepo.save(c);
  }
}

package com.redthread.order.controller;

import com.redthread.order.dto.AddItemReq;
import com.redthread.order.dto.CartRes;
import com.redthread.order.dto.UpdateItemReq;
import com.redthread.order.dto.UpdateQtyReq;
import com.redthread.order.security.JwtUserResolver;
import com.redthread.order.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Carrito del usuario autenticado")
public class CartController {

  private final CartService cartService;
  private final JwtUserResolver auth;

  @Operation(summary = "Crear u obtener carrito actual")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Carrito obtenido",
          content = @Content(schema = @Schema(implementation = CartRes.class))),
      @ApiResponse(responseCode = "401", description = "No autenticado")
  })
  @PostMapping
  public CartRes createOrGet() {
    return cartService.getCart(auth.currentUserId());
  }

  @Operation(summary = "Detalle del carrito actual")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Carrito obtenido",
          content = @Content(schema = @Schema(implementation = CartRes.class))),
      @ApiResponse(responseCode = "401", description = "No autenticado")
  })
  @GetMapping
  public CartRes detail() {
    return cartService.getCart(auth.currentUserId());
  }

  @Operation(summary = "Agregar item al carrito")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Item agregado",
          content = @Content(schema = @Schema(implementation = CartRes.class))),
      @ApiResponse(responseCode = "400", description = "Datos inválidos"),
      @ApiResponse(responseCode = "409", description = "Sin stock o precio no disponible"),
      @ApiResponse(responseCode = "401", description = "No autenticado")
  })
  @PostMapping("/items")
  public CartRes addItem(@Valid @RequestBody AddItemReq req) {
    return cartService.addItem(auth.currentUserId(), req);
  }

  @Operation(summary = "Actualizar cantidad de item por itemId interno")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Cantidad actualizada",
          content = @Content(schema = @Schema(implementation = CartRes.class))),
      @ApiResponse(responseCode = "400", description = "Item no encontrado o qty inválida"),
      @ApiResponse(responseCode = "401", description = "No autenticado")
  })
  @PatchMapping("/items/{itemId}")
  public CartRes updateItem(
      @Parameter(description = "ID interno del item", example = "5")
      @PathVariable Long itemId,
      @Valid @RequestBody UpdateQtyReq req
  ) {
    return cartService.updateItem(auth.currentUserId(), itemId, req);
  }

  @Operation(summary = "Eliminar item por itemId interno")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Item eliminado"),
      @ApiResponse(responseCode = "401", description = "No autenticado")
  })
  @DeleteMapping("/items/{itemId}")
  public void deleteItem(
      @Parameter(description = "ID interno del item", example = "5")
      @PathVariable Long itemId
  ) {
    cartService.removeItem(auth.currentUserId(), itemId);
  }

  // ====== ALIAS legacy para app ======

  @Operation(summary = "Alias: agregar item (compatibilidad app)",
      description = "Igual que POST /cart/items")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Item agregado",
          content = @Content(schema = @Schema(implementation = CartRes.class)))
  })
  @PostMapping("/add")
  public CartRes addAlias(@Valid @RequestBody AddItemReq req) {
    return cartService.addItem(auth.currentUserId(), req);
  }

  @Operation(summary = "Alias: actualizar item (compatibilidad app)",
      description = "Recibe {itemId, quantity} y actualiza qty. Igual que PATCH /cart/items/{itemId}")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Cantidad actualizada",
          content = @Content(schema = @Schema(implementation = CartRes.class))),
      @ApiResponse(responseCode = "400", description = "Item no encontrado o qty inválida")
  })
  @PostMapping("/update")
  public CartRes updateAlias(@Valid @RequestBody UpdateItemReq req) {
    return cartService.updateItem(
        auth.currentUserId(),
        req.itemId(),
        new UpdateQtyReq(req.quantity())
    );
  }

  @Operation(summary = "Eliminar item por variantId (legacy)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Item eliminado")
  })
  @DeleteMapping("/item/{productId}")
  public void deleteByVariant(
      @Parameter(description = "VariantId real del catálogo", example = "10")
      @PathVariable("productId") Long variantId
  ) {
    cartService.removeItemByVariant(auth.currentUserId(), variantId);
  }

  @Operation(summary = "Vaciar carrito completo")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Carrito vaciado")
  })
  @DeleteMapping("/clear")
  public void clear() {
    cartService.clearCart(auth.currentUserId());
  }
}

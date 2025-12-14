package com.redthread.order.controller;

import com.redthread.order.dto.CheckoutReq;
import com.redthread.order.dto.OrderItemRes;
import com.redthread.order.dto.OrderRes;
import com.redthread.order.model.Order;
import com.redthread.order.repository.OrderItemRepository;
import com.redthread.order.security.JwtUserResolver;
import com.redthread.order.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "Checkout desde carrito a orden")
public class CheckoutController {

  private final CheckoutService checkoutService;
  private final JwtUserResolver auth;
  private final OrderItemRepository orderItemRepo;

  @Operation(summary = "Checkout: crear orden desde carrito y vaciar carrito")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Orden creada",
          content = @Content(schema = @Schema(implementation = OrderRes.class))),
      @ApiResponse(responseCode = "400", description = "Carrito o dirección no existe"),
      @ApiResponse(responseCode = "409", description = "Carrito vacío o sin stock"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "500", description = "Error interno al procesar checkout")

  })
  @PostMapping("/checkout")
  public OrderRes checkout(@Valid @RequestBody CheckoutReq req) {

    Order o = checkoutService.checkout(auth.currentUserId(), req);
    var items = orderItemRepo.findByOrderId(o.getId());

    return new OrderRes(
        o.getId(),
        o.getStatus().name(),
        o.getTotalAmount(),
        items.stream()
            .map(i -> new OrderItemRes(
                i.getVariantId(),
                i.getQuantity(),
                i.getUnitPrice(),
                i.getLineTotal()
            ))
            .collect(Collectors.toList())
    );
  }
}

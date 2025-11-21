package com.redthread.order.controller;

import com.redthread.order.dto.CheckoutReq;
import com.redthread.order.dto.OrderItemRes;
import com.redthread.order.dto.OrderRes;
import com.redthread.order.model.Order;
import com.redthread.order.repository.OrderItemRepository;
import com.redthread.order.security.JwtUserResolver;
import com.redthread.order.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CheckoutController {

  private final CheckoutService checkoutService;
  private final JwtUserResolver auth;
  private final OrderItemRepository orderItemRepo;

  @PostMapping("/checkout")
  public OrderRes checkout(@Valid @RequestBody CheckoutReq req) {
    System.out.println("🔹 Checkout recibido correctamente con addressId=" + req.addressId());
    try {

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

    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }
}

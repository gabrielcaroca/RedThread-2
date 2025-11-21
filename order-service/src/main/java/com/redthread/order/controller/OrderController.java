package com.redthread.order.controller;

import com.redthread.order.dto.OrderItemRes;
import com.redthread.order.dto.OrderRes;
import com.redthread.order.dto.PayReq;
import com.redthread.order.model.Order;
import com.redthread.order.security.JwtUserResolver;
import com.redthread.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final JwtUserResolver auth;

  @GetMapping
  public List<OrderRes> list() {
    return orderService.listByUser(auth.currentUserId()).stream().map(o ->
        new OrderRes(
            o.getId(),
            o.getStatus().name(),
            o.getTotalAmount(),
            o.getItems().stream().map(i ->
                new OrderItemRes(
                    i.getVariantId(),
                    i.getQuantity(),
                    i.getUnitPrice(),
                    i.getLineTotal()
                )
            ).toList()
        )
    ).toList();
  }

  @GetMapping("/{id}")
  public OrderRes detail(@PathVariable Long id) {
    Order o = orderService.getByIdForUser(id, auth.currentUserId());
    return new OrderRes(
        o.getId(),
        o.getStatus().name(),
        o.getTotalAmount(),
        o.getItems().stream().map(i ->
            new OrderItemRes(
                i.getVariantId(),
                i.getQuantity(),
                i.getUnitPrice(),
                i.getLineTotal()
            )
        ).toList()
    );
  }

  @PostMapping("/{id}/pay")
  public OrderRes pay(@PathVariable Long id, @Valid @RequestBody(required = false) PayReq req) {
    String provider = (req == null) ? null : req.provider();
    Order o = orderService.pay(auth.currentUserId(), id, provider);
    return new OrderRes(
        o.getId(),
        o.getStatus().name(),
        o.getTotalAmount(),
        o.getItems().stream().map(i ->
            new OrderItemRes(
                i.getVariantId(),
                i.getQuantity(),
                i.getUnitPrice(),
                i.getLineTotal()
            )
        ).toList()
    );
  }

  @PostMapping("/{id}/cancel")
  public OrderRes cancel(@PathVariable Long id) {
    Order o = orderService.cancel(auth.currentUserId(), id);
    return new OrderRes(
        o.getId(),
        o.getStatus().name(),
        o.getTotalAmount(),
        o.getItems().stream().map(i ->
            new OrderItemRes(
                i.getVariantId(),
                i.getQuantity(),
                i.getUnitPrice(),
                i.getLineTotal()
            )
        ).toList()
    );
  }
}

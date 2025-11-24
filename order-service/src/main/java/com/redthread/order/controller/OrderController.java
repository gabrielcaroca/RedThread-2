package com.redthread.order.controller;

import com.redthread.order.dto.*;
import com.redthread.order.model.Order;
import com.redthread.order.security.JwtUserResolver;
import com.redthread.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

  @GetMapping("/{id}/delivery")
  public OrderDeliveryRes deliveryDetail(@PathVariable Long id) {
    Order o = orderService.getByIdForUser(id, auth.currentUserId());

    var a = o.getAddress();
    AddressRes addr = new AddressRes(
        a.getId(),
        a.getLine1(),
        a.getLine2(),
        a.getCity(),
        a.getState(),
        a.getZip(),
        a.getCountry(),
        a.isDefault()
    );

    return new OrderDeliveryRes(
        o.getId(),
        o.getStatus().name(),
        o.getTotalAmount(),
        o.getUserId(),
        addr,
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

  @PostMapping("/{id}/delivery-status")
  public void deliveryStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
    String status = body.get("status");
    String note = body.get("note");
    orderService.updateDeliveryStatusInternal(id, status, note);
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

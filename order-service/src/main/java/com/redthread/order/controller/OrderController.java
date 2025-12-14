package com.redthread.order.controller;

import com.redthread.order.dto.*;
import com.redthread.order.model.Address;
import com.redthread.order.model.Order;
import com.redthread.order.security.JwtUserResolver;
import com.redthread.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Endpoints de órdenes + integración con delivery-service")
public class OrderController {

        private final OrderService orderService;
        private final JwtUserResolver auth;

        // -------------------------------------------------------
        @Operation(summary = "Listar órdenes del usuario")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Órdenes obtenidas"),
                        @ApiResponse(responseCode = "401", description = "No autenticado"),
                        @ApiResponse(responseCode = "500", description = "Error interno")
        })
        @GetMapping
        public List<OrderRes> list() {
                return orderService.listByUser(auth.currentUserId())
                                .stream()
                                .map(o -> new OrderRes(
                                                o.getId(),
                                                o.getStatus().name(),
                                                o.getTotalAmount(),
                                                o.getItems().stream().map(i -> new OrderItemRes(
                                                                i.getVariantId(),
                                                                i.getQuantity(),
                                                                i.getUnitPrice(),
                                                                i.getLineTotal())).toList()))
                                .toList();
        }

        // -------------------------------------------------------
        @Operation(summary = "Detalle de orden del usuario")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Orden encontrada"),
                        @ApiResponse(responseCode = "400", description = "No encontrada")
        })
        @GetMapping("/{id}")
        public OrderRes detail(@PathVariable Long id) {
                Order o = orderService.getByIdForUser(id, auth.currentUserId());
                return new OrderRes(
                                o.getId(),
                                o.getStatus().name(),
                                o.getTotalAmount(),
                                o.getItems().stream().map(i -> new OrderItemRes(
                                                i.getVariantId(),
                                                i.getQuantity(),
                                                i.getUnitPrice(),
                                                i.getLineTotal())).toList());
        }

        // -------------------------------------------------------
        @Operation(summary = "Detalle para delivery-service", description = "Usado por delivery-service para generar rutas")
        @ApiResponse(responseCode = "200", description = "OK")
        @GetMapping("/{id}/delivery")
        public OrderDeliveryRes deliveryDetail(@PathVariable Long id) {

                Order o = orderService.getByIdForUser(id, auth.currentUserId());
                Address a = o.getAddress();

                AddressRes addr = new AddressRes(
                                a.getId(),
                                a.getLine1(),
                                a.getLine2(),
                                a.getCity(),
                                a.getState(),
                                a.getZip(),
                                a.getCountry(),
                                a.isDefault());

                return new OrderDeliveryRes(
                                o.getId(),
                                o.getStatus().name(),
                                o.getTotalAmount(),
                                o.getUserId(),
                                addr,
                                o.getItems().stream().map(i -> new OrderItemRes(
                                                i.getVariantId(),
                                                i.getQuantity(),
                                                i.getUnitPrice(),
                                                i.getLineTotal())).toList());
        }

        // -------------------------------------------------------
        @Operation(summary = "Actualizar estado de delivery-service → order-service")
        @PostMapping("/{id}/delivery-status")
        public void deliveryStatus(
                        @PathVariable Long id,
                        @RequestBody Map<String, String> body) {
                String status = body.get("status");
                String note = body.get("note");
                orderService.updateDeliveryStatusInternal(id, status, note);
        }

        // -------------------------------------------------------
        @Operation(summary = "Pagar orden")
        @PostMapping("/{id}/pay")
        public OrderRes pay(
                        @PathVariable Long id,
                        @Valid @RequestBody(required = false) PayReq req) {
                String provider = req == null ? null : req.provider();

                Order o = orderService.pay(auth.currentUserId(), id, provider);

                return new OrderRes(
                                o.getId(),
                                o.getStatus().name(),
                                o.getTotalAmount(),
                                o.getItems().stream().map(i -> new OrderItemRes(
                                                i.getVariantId(),
                                                i.getQuantity(),
                                                i.getUnitPrice(),
                                                i.getLineTotal())).toList());
        }

        // -------------------------------------------------------
        @Operation(summary = "Cancelar orden")
        @PostMapping("/{id}/cancel")
        public OrderRes cancel(@PathVariable Long id) {
                Order o = orderService.cancel(auth.currentUserId(), id);

                return new OrderRes(
                                o.getId(),
                                o.getStatus().name(),
                                o.getTotalAmount(),
                                o.getItems().stream().map(i -> new OrderItemRes(
                                                i.getVariantId(),
                                                i.getQuantity(),
                                                i.getUnitPrice(),
                                                i.getLineTotal())).toList());
        }

        @Operation(summary = "Detalle de orden (ADMIN)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Detalle de orden para admin"),
                        @ApiResponse(responseCode = "404", description = "Orden no encontrada")
        })
        @GetMapping("/admin/{id}")
        public AdminOrderDetailRes adminDetail(@PathVariable Long id) {
                return orderService.getAdminDetail(id);
        }

}

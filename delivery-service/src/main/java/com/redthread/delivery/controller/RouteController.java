package com.redthread.delivery.controller;

import com.redthread.delivery.domain.DeliveryRoute;
import com.redthread.delivery.domain.Shipment;
import com.redthread.delivery.dto.route.CreateRouteRequest;
import com.redthread.delivery.dto.route.RouteResponse;
import com.redthread.delivery.dto.shipment.ShipmentResponse;
import com.redthread.delivery.security.AuthUtils;
import com.redthread.delivery.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;
    private final AuthUtils auth;

    @PostMapping
    public RouteResponse create(@Valid @RequestBody CreateRouteRequest req,
                                @AuthenticationPrincipal Jwt jwt) {

        if (!auth.hasAdmin(jwt)) throw new SecurityException("Only admin can create routes");

        Long adminId = auth.getCurrentUserId(jwt);

        DeliveryRoute r = routeService.create(req, adminId, jwt);

        return map(r);
    }

    @GetMapping("/active")
    public List<RouteResponse> active(@AuthenticationPrincipal Jwt jwt) {
        return routeService.activeRoutes().stream().map(this::map).toList();
    }

    @PostMapping("/{id}/take")
    public RouteResponse take(@PathVariable Long id,
                              @AuthenticationPrincipal Jwt jwt) {

        Long driverId = auth.getCurrentUserId(jwt);
        DeliveryRoute r = routeService.takeRoute(id, driverId);
        return map(r);
    }

    @GetMapping("/{id}/shipments")
    public List<ShipmentResponse> shipments(@PathVariable Long id,
                                            @AuthenticationPrincipal Jwt jwt) {

        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);

        List<Shipment> list = routeService.shipmentsByRoute(id, userId, isAdmin);
        return list.stream().map(ShipmentResponse::from).toList();
    }

    private RouteResponse map(DeliveryRoute r) {
        return new RouteResponse(
                r.getId(),
                r.getNombre(),
                r.getDescripcion(),
                r.getTotalPedidos(),
                r.getTotalPrice(),
                r.getActiva(),
                r.getAssignedUserId()
        );
    }
}

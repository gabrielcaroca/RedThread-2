package com.redthread.delivery.service;

import com.redthread.delivery.domain.DeliveryRoute;
import com.redthread.delivery.domain.Shipment;
import com.redthread.delivery.dto.route.CreateRouteRequest;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public interface RouteService {

    DeliveryRoute create(CreateRouteRequest req, Long adminId, Jwt jwt);

    List<DeliveryRoute> activeRoutes();

    DeliveryRoute takeRoute(Long routeId, Long driverId);

    List<Shipment> shipmentsByRoute(Long routeId, Long userId, boolean isAdmin);
}

package com.redthread.delivery.service.impl;

import com.redthread.delivery.domain.DeliveryRoute;
import com.redthread.delivery.domain.DeliveryStatus;
import com.redthread.delivery.domain.Shipment;
import com.redthread.delivery.dto.route.CreateRouteRequest;
import com.redthread.delivery.integration.OrderClient;
import com.redthread.delivery.integration.dto.OrderDeliveryResponse;
import com.redthread.delivery.integration.dto.ShippingAddress;
import com.redthread.delivery.repository.DeliveryRouteRepository;
import com.redthread.delivery.repository.ShipmentRepository;
import com.redthread.delivery.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final DeliveryRouteRepository routeRepo;
    private final ShipmentRepository shipmentRepo;
    private final OrderClient orderClient;

    @Value("${app.rates.fixed:1900}")
    private BigDecimal fixedPrice;

    @Override
    @Transactional
    public DeliveryRoute create(CreateRouteRequest req, Long adminId, Jwt jwt) {

        int totalPedidos = req.orderIds().size();
        long totalPriceRoute = (req.totalPrice() != null)
                ? req.totalPrice()
                : fixedPrice.multiply(BigDecimal.valueOf(totalPedidos)).longValue();

        DeliveryRoute route = DeliveryRoute.builder()
                .nombre(req.nombre())
                .descripcion(req.descripcion() == null ? "" : req.descripcion())
                .totalPedidos(totalPedidos)
                .totalPrice(totalPriceRoute)
                .activa(true)
                .assignedUserId(null)
                .build();

        route = routeRepo.save(route);

        for (Long orderId : req.orderIds()) {

            OrderDeliveryResponse info = orderClient.getDeliveryInfo(orderId, jwt)
                    .blockOptional()
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

            ShippingAddress addr = info.shippingAddress();
            if (addr == null) {
                throw new IllegalArgumentException("Order missing shippingAddress: " + orderId);
            }

            Shipment sh = Shipment.builder()
                    .orderId(orderId)
                    .userId(info.userId())
                    .addressLine1(addr.line1())
                    .addressLine2(addr.line2())
                    .city(addr.city())
                    .state(addr.state())
                    .zip(addr.zip())
                    .country(addr.country())
                    .status(DeliveryStatus.PENDING_PICKUP)
                    .assignedUserId(null)
                    .totalPrice(fixedPrice)
                    .route(route)
                    .build();

            shipmentRepo.save(sh);
        }

        return route;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryRoute> activeRoutes() {
        return routeRepo.findByActivaTrueAndAssignedUserIdIsNull();
    }

    @Override
    @Transactional
    public DeliveryRoute takeRoute(Long routeId, Long driverId) {

        routeRepo.findByAssignedUserId(driverId)
                .filter(DeliveryRoute::getActiva)
                .ifPresent(r -> { throw new IllegalStateException("Ya tienes una ruta asignada"); });

        DeliveryRoute route = routeRepo.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Ruta no existe"));

        if (!Boolean.TRUE.equals(route.getActiva()) || route.getAssignedUserId() != null) {
            throw new IllegalStateException("Ruta no disponible");
        }

        route.setAssignedUserId(driverId);
        routeRepo.save(route);

        List<Shipment> shipments = shipmentRepo.findByRouteId(routeId);
        for (Shipment s : shipments) {
            s.setAssignedUserId(driverId);
            if (s.getStatus() == DeliveryStatus.PENDING_PICKUP) {
                s.setStatus(DeliveryStatus.ASSIGNED);
            }
            shipmentRepo.save(s);
        }

        return route;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shipment> shipmentsByRoute(Long routeId, Long userId, boolean isAdmin) {
        DeliveryRoute route = routeRepo.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Ruta no existe"));

        if (!isAdmin) {
            if (route.getAssignedUserId() == null || !route.getAssignedUserId().equals(userId)) {
                throw new SecurityException("No autorizado a ver esta ruta.");
            }
        }

        return shipmentRepo.findByRouteId(routeId);
    }
}

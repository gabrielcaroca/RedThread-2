package com.redthread.delivery.service;

import com.redthread.delivery.domain.DeliveryRoute;
import com.redthread.delivery.domain.DeliveryStatus;
import com.redthread.delivery.domain.Shipment;
import com.redthread.delivery.integration.OrderClient;
import com.redthread.delivery.repository.DeliveryRouteRepository;
import com.redthread.delivery.repository.ShipmentRepository;
import com.redthread.delivery.service.impl.RouteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RouteServiceImplTest {

    private DeliveryRouteRepository routeRepo;
    private ShipmentRepository shipmentRepo;
    private OrderClient orderClient;
    private RouteServiceImpl service;

    @BeforeEach
    void setup() {
        routeRepo = mock(DeliveryRouteRepository.class);
        shipmentRepo = mock(ShipmentRepository.class);
        orderClient = mock(OrderClient.class);
        service = new RouteServiceImpl(routeRepo, shipmentRepo, orderClient);
    }

    @Test
    void takeRoute_assignsDriver_andUpdatesShipments() {
        Long routeId = 10L;
        Long driverId = 55L;

        DeliveryRoute route = DeliveryRoute.builder()
                .id(routeId)
                .activa(true)
                .assignedUserId(null)
                .build();

        Shipment s1 = Shipment.builder().id(1L).status(DeliveryStatus.PENDING_PICKUP).assignedUserId(null).build();
        Shipment s2 = Shipment.builder().id(2L).status(DeliveryStatus.ASSIGNED).assignedUserId(null).build();

        when(routeRepo.findByAssignedUserId(driverId)).thenReturn(Optional.empty());
        when(routeRepo.findById(routeId)).thenReturn(Optional.of(route));
        when(shipmentRepo.findByRouteId(routeId)).thenReturn(List.of(s1, s2));

        DeliveryRoute out = service.takeRoute(routeId, driverId);

        assertEquals(driverId, out.getAssignedUserId());
        assertEquals(DeliveryStatus.ASSIGNED, s1.getStatus());
        assertEquals(driverId, s1.getAssignedUserId());
        assertEquals(driverId, s2.getAssignedUserId());

        verify(routeRepo).save(route);
        verify(shipmentRepo, times(2)).save(any(Shipment.class));
    }
}

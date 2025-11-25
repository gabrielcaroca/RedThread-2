package com.redthread.delivery.service;

import com.redthread.delivery.domain.DeliveryStatus;
import com.redthread.delivery.domain.Shipment;
import com.redthread.delivery.domain.TrackingEvent;
import com.redthread.delivery.integration.OrderClient;
import com.redthread.delivery.repository.ShipmentAssignmentRepository;
import com.redthread.delivery.repository.ShipmentRepository;
import com.redthread.delivery.repository.TrackingEventRepository;
import com.redthread.delivery.service.impl.ShipmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShipmentServiceImplTest {

    private ShipmentRepository shipmentRepo;
    private TrackingEventRepository trackingRepo;
    private ShipmentAssignmentRepository assignmentRepo;
    private OrderClient orderClient;
    private ShipmentServiceImpl service;

    @BeforeEach
    void setup() {
        shipmentRepo = mock(ShipmentRepository.class);
        trackingRepo = mock(TrackingEventRepository.class);
        assignmentRepo = mock(ShipmentAssignmentRepository.class);
        orderClient = mock(OrderClient.class);

        // ✅ ORDEN REAL DEL CONSTRUCTOR:
        service = new ShipmentServiceImpl(shipmentRepo, trackingRepo, assignmentRepo, orderClient);
    }

    @Test
    void cancel_setsCancelled_whenNotCompleted() {
        Shipment s = Shipment.builder()
                .id(1L)
                .userId(99L)
                .status(DeliveryStatus.ASSIGNED)
                .build();

        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(s));
        when(shipmentRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Shipment out = service.cancel(1L, 99L, false);

        assertEquals(DeliveryStatus.CANCELLED, out.getStatus());
        verify(trackingRepo).save(any(TrackingEvent.class));
    }

    @Test
    void cancel_throws_whenDelivered() {
        Shipment s = Shipment.builder()
                .id(1L)
                .userId(99L)
                .status(DeliveryStatus.DELIVERED)
                .build();

        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(s));

        assertThrows(IllegalStateException.class,
                () -> service.cancel(1L, 99L, false));
    }
}

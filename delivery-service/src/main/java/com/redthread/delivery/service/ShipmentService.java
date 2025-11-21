package com.redthread.delivery.service;

import com.redthread.delivery.domain.*;
import com.redthread.delivery.dto.shipment.*;
import java.util.List;

public interface ShipmentService {
    Shipment createFromOrder(CreateShipmentRequest req, Long currentUserId, org.springframework.security.oauth2.jwt.Jwt jwt);
    List<Shipment> listMine(Long currentUserId, boolean isAdmin);
    Shipment getFor(Long id, Long currentUserId, boolean isAdmin);
    Shipment start(Long id, Long currentUserId, boolean isAdmin);
    Shipment delivered(Long id, String note, Long currentUserId, boolean isAdmin, org.springframework.security.oauth2.jwt.Jwt jwt);
    Shipment fail(Long id, String note, Long currentUserId, boolean isAdmin, org.springframework.security.oauth2.jwt.Jwt jwt);
    Shipment cancel(Long id, Long currentUserId, boolean isAdmin);
    TrackingEvent track(Long id, TrackRequest req, Long currentUserId, boolean isAdmin);
    List<TrackingEvent> listTracking(Long id, Long currentUserId, boolean isAdmin);
}

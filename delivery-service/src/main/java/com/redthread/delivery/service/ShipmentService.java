package com.redthread.delivery.service;

import com.redthread.delivery.domain.Shipment;
import com.redthread.delivery.domain.TrackingEvent;
import com.redthread.delivery.dto.shipment.CreateShipmentRequest;
import com.redthread.delivery.dto.shipment.TrackRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ShipmentService {

    Shipment createFromOrder(CreateShipmentRequest req, Long currentUserId, Jwt jwt);

    List<Shipment> listMine(Long currentUserId, boolean isAdmin);

    Shipment getFor(Long id, Long currentUserId, boolean isAdmin);

    Shipment start(Long id, Long currentUserId, boolean isAdmin);

    Shipment delivered(Long id,
                       String receiverName,
                       String note,
                       MultipartFile photo,
                       BigDecimal latitude,
                       BigDecimal longitude,
                       Long currentUserId,
                       boolean isAdmin,
                       Jwt jwt);

    Shipment fail(Long id,
                  String note,
                  MultipartFile photo,
                  BigDecimal latitude,
                  BigDecimal longitude,
                  Long currentUserId,
                  boolean isAdmin,
                  Jwt jwt);

    Shipment cancel(Long id, Long currentUserId, boolean isAdmin);

    TrackingEvent track(Long id, TrackRequest req, Long currentUserId, boolean isAdmin);

    List<TrackingEvent> listTracking(Long id, Long currentUserId, boolean isAdmin);

    List<Shipment> listAssignedToMe(Long currentUserId, boolean isAdmin);
}

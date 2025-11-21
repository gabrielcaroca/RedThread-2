package com.redthread.delivery.service.impl;

import com.redthread.delivery.domain.*;
import com.redthread.delivery.dto.shipment.*;
import com.redthread.delivery.integration.OrderClient;
import com.redthread.delivery.repository.*;
import com.redthread.delivery.service.ShipmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

@Service @Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepo;
    private final GeoZoneRepository zoneRepo;
    private final RateRepository rateRepo;
    private final TrackingEventRepository trackingRepo;
    private final OrderClient orderClient;

    @Value("${app.rates.default-base:2990}")
    private BigDecimal defaultBase;

    @Value("${app.webhook.enabled:false}")
    private boolean webhookEnabled;

    public ShipmentServiceImpl(ShipmentRepository shipmentRepo, GeoZoneRepository zoneRepo, RateRepository rateRepo,
                               TrackingEventRepository trackingRepo, OrderClient orderClient) {
        this.shipmentRepo = shipmentRepo;
        this.zoneRepo = zoneRepo;
        this.rateRepo = rateRepo;
        this.trackingRepo = trackingRepo;
        this.orderClient = orderClient;
    }

    @Override
    public Shipment createFromOrder(CreateShipmentRequest req, Long currentUserId, Jwt jwt) {
        // 1) Llamar a Order-Service con el mismo bearer
        Map<String, Object> order = orderClient.getOrderById(req.orderId(), jwt).blockOptional()
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // 2) Verificar owner
        Long ownerId = extractLong(order.get("userId"));
        if (ownerId == null || !ownerId.equals(currentUserId)) {
            throw new SecurityException("Order does not belong to current user");
        }

        // 3) Direccion desde order
        Map<String, Object> address = (Map<String, Object>) order.get("shippingAddress");
        if (address == null) throw new IllegalArgumentException("Order missing shipping address");

        String line1 = Objects.toString(address.get("line1"), null);
        String line2 = (String) address.get("line2");
        String city = Objects.toString(address.get("city"), "");
        String state = (String) address.get("state");
        String zip = (String) address.get("zip");
        String country = Objects.toString(address.get("country"), "CL");
        if (line1 == null || city.isBlank() || country.isBlank())
            throw new IllegalArgumentException("Invalid address");

        // 4) Determinar zona/tarifa
        GeoZone zone = resolveZone(city, zip);
        BigDecimal price = zone != null ? resolveRate(zone) : defaultBase;

        Shipment s = Shipment.builder()
                .orderId(req.orderId())
                .userId(ownerId)
                .addressLine1(line1)
                .addressLine2(line2)
                .city(city)
                .state(state)
                .zip(zip)
                .country(country)
                .zone(zone)
                .status(DeliveryStatus.PENDING_PICKUP)
                .totalPrice(price)
                .build();
        return shipmentRepo.save(s);
    }

    private GeoZone resolveZone(String city, String zip) {
        List<GeoZone> zones = zoneRepo.findAll();
        for (GeoZone z : zones) {
            boolean matchCity = (z.getCity() != null && !z.getCity().isBlank() && z.getCity().equalsIgnoreCase(city));
            boolean matchZip = false;
            if (z.getZipPattern() != null && !z.getZipPattern().isBlank() && zip != null) {
                matchZip = Pattern.compile(z.getZipPattern()).matcher(zip).matches();
            }
            if (matchCity || matchZip) return z;
        }
        return null;
    }

    private BigDecimal resolveRate(GeoZone zone) {
        return rateRepo.findByZoneIdAndIsActiveTrue(zone.getId()).stream()
                .findFirst()
                .map(Rate::getBasePrice)
                .orElse(defaultBase);
    }

    private Long extractLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return null; }
    }

    @Override @Transactional(readOnly = true)
    public List<Shipment> listMine(Long currentUserId, boolean isAdmin) {
        if (isAdmin) return shipmentRepo.findAll();
        return shipmentRepo.findByUserIdOrderByCreatedAtDesc(currentUserId);
    }

    @Override @Transactional(readOnly = true)
    public Shipment getFor(Long id, Long currentUserId, boolean isAdmin) {
        Shipment s = shipmentRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Shipment not found"));
        if (!isAdmin && !Objects.equals(s.getUserId(), currentUserId))
            throw new SecurityException("Forbidden");
        return s;
    }

    @Override
    public Shipment start(Long id, Long currentUserId, boolean isAdmin) {
        Shipment s = getFor(id, currentUserId, isAdmin);
        s.setStatus(DeliveryStatus.IN_TRANSIT);
        return shipmentRepo.save(s);
    }

    @Override
    public Shipment delivered(Long id, String note, Long currentUserId, boolean isAdmin, Jwt jwt) {
        Shipment s = getFor(id, currentUserId, isAdmin);
        s.setStatus(DeliveryStatus.DELIVERED);
        shipmentRepo.save(s);
        TrackingEvent te = TrackingEvent.builder()
                .shipment(s).status(DeliveryStatus.DELIVERED).note(note).build();
        trackingRepo.save(te);
        if (webhookEnabled) orderClient.postDeliveryStatus(s.getOrderId(), "DELIVERED", note, jwt).subscribe();
        return s;
    }

    @Override
    public Shipment fail(Long id, String note, Long currentUserId, boolean isAdmin, Jwt jwt) {
        Shipment s = getFor(id, currentUserId, isAdmin);
        s.setStatus(DeliveryStatus.FAILED);
        shipmentRepo.save(s);
        TrackingEvent te = TrackingEvent.builder()
                .shipment(s).status(DeliveryStatus.FAILED).note(note).build();
        trackingRepo.save(te);
        if (webhookEnabled) orderClient.postDeliveryStatus(s.getOrderId(), "FAILED", note, jwt).subscribe();
        return s;
    }

    @Override
    public Shipment cancel(Long id, Long currentUserId, boolean isAdmin) {
        Shipment s = getFor(id, currentUserId, isAdmin);
        if (s.getStatus() == DeliveryStatus.DELIVERED || s.getStatus() == DeliveryStatus.FAILED)
            throw new IllegalStateException("Cannot cancel a completed shipment");
        s.setStatus(DeliveryStatus.CANCELLED);
        return shipmentRepo.save(s);
    }

    @Override
    public TrackingEvent track(Long id, TrackRequest req, Long currentUserId, boolean isAdmin) {
        Shipment s = getFor(id, currentUserId, isAdmin);
        if (req.status() != null) {
            s.setStatus(req.status());
            shipmentRepo.save(s);
        }
        var te = TrackingEvent.builder()
                .shipment(s)
                .status(req.status())
                .note(req.note())
                .build();
        if (req.latitude() != null && !req.latitude().isBlank())
            te.setLatitude(new java.math.BigDecimal(req.latitude()));
        if (req.longitude() != null && !req.longitude().isBlank())
            te.setLongitude(new java.math.BigDecimal(req.longitude()));
        return trackingRepo.save(te);
    }

    @Override @Transactional(readOnly = true)
    public List<TrackingEvent> listTracking(Long id, Long currentUserId, boolean isAdmin) {
        Shipment s = getFor(id, currentUserId, isAdmin);
        return trackingRepo.findByShipmentIdOrderByCreatedAtAsc(s.getId());
    }
}

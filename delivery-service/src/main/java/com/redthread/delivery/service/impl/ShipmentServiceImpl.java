package com.redthread.delivery.service.impl;

import com.redthread.delivery.domain.*;
import com.redthread.delivery.dto.shipment.CreateShipmentRequest;
import com.redthread.delivery.dto.shipment.TrackRequest;
import com.redthread.delivery.integration.OrderClient;
import com.redthread.delivery.repository.ShipmentAssignmentRepository;
import com.redthread.delivery.repository.ShipmentRepository;
import com.redthread.delivery.repository.TrackingEventRepository;
import com.redthread.delivery.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepo;
    private final TrackingEventRepository trackingRepo;
    private final ShipmentAssignmentRepository assignmentRepo;
    private final OrderClient orderClient;

    @Value("${app.rates.fixed:1900}")
    private BigDecimal fixedPrice;

    @Value("${app.webhook.enabled:false}")
    private boolean webhookEnabled;

    @Value("${app.evidence.dir:./evidence}")
    private String evidenceDir;

    @Override
    @SuppressWarnings("unchecked")
    public Shipment createFromOrder(CreateShipmentRequest req, Long currentUserId, Jwt jwt) {

        Map<String, Object> order = orderClient.getOrderById(req.orderId(), jwt)
                .blockOptional()
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        Long ownerId = extractLong(order.get("userId"));
        if (ownerId == null || !ownerId.equals(currentUserId)) {
            throw new SecurityException("Order does not belong to current user");
        }

        Map<String, Object> address = (Map<String, Object>) order.get("shippingAddress");
        if (address == null) throw new IllegalArgumentException("Order missing shipping address");

        String line1 = (String) address.get("addressLine1");
        String line2 = (String) address.get("addressLine2");
        String city = (String) address.get("city");
        String state = (String) address.get("state");
        String zip = (String) address.get("zip");
        String country = Objects.toString(address.get("country"), "CL");

        if (line1 == null || city == null || city.isBlank() || country.isBlank()) {
            throw new IllegalArgumentException("Invalid address");
        }

        Shipment s = Shipment.builder()
                .orderId(req.orderId())
                .userId(ownerId)
                .addressLine1(line1)
                .addressLine2(line2)
                .city(city)
                .state(state)
                .zip(zip)
                .country(country)
                .status(DeliveryStatus.PENDING_PICKUP)
                .totalPrice(fixedPrice)
                .build();

        return shipmentRepo.save(s);
    }

    private Long extractLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); }
        catch (Exception e) { return null; }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shipment> listMine(Long currentUserId, boolean isAdmin) {
        if (isAdmin) return shipmentRepo.findAll();
        return shipmentRepo.findByUserIdOrderByCreatedAtDesc(currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Shipment getFor(Long id, Long currentUserId, boolean isAdmin) {
        Shipment s = shipmentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found"));

        if (isAdmin) return s;

        if (Objects.equals(s.getUserId(), currentUserId)) return s;

        if (s.getAssignedUserId() != null && s.getAssignedUserId().equals(currentUserId)) return s;

        boolean assigned = assignmentRepo.existsByAssignedUserIdAndShipmentId(currentUserId, s.getId());
        if (!assigned) throw new SecurityException("Forbidden");

        return s;
    }

    @Override
    public Shipment start(Long id, Long currentUserId, boolean isAdmin) {
        Shipment s = getFor(id, currentUserId, isAdmin);
        s.setStatus(DeliveryStatus.IN_TRANSIT);
        Shipment saved = shipmentRepo.save(s);

        trackingRepo.save(TrackingEvent.builder()
                .shipment(saved)
                .status(DeliveryStatus.IN_TRANSIT)
                .note("Picked up / started")
                .build());

        return saved;
    }

    private String storeEvidenceFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Photo is required");
        try {
            Path dir = Paths.get(evidenceDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String name = UUID.randomUUID() + (ext == null ? "" : ("." + ext));
            Path target = dir.resolve(name);

            try (var in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/evidence/" + name;
        } catch (IOException e) {
            throw new RuntimeException("Could not store evidence file", e);
        }
    }

    @Override
    public Shipment delivered(Long id,
                             String receiverName,
                             String note,
                             MultipartFile photo,
                             BigDecimal latitude,
                             BigDecimal longitude,
                             Long currentUserId,
                             boolean isAdmin,
                             Jwt jwt) {
        Shipment s = getFor(id, currentUserId, isAdmin);

        if (receiverName == null || receiverName.isBlank()) {
            throw new IllegalArgumentException("receiverName is required");
        }

        String evidenceUrl = storeEvidenceFile(photo);

        s.setStatus(DeliveryStatus.DELIVERED);
        s.setReceiverName(receiverName.trim());
        s.setNote(note);
        s.setEvidenceUrl(evidenceUrl);

        Shipment saved = shipmentRepo.save(s);

        trackingRepo.save(TrackingEvent.builder()
                .shipment(saved)
                .status(DeliveryStatus.DELIVERED)
                .latitude(latitude)
                .longitude(longitude)
                .note(note)
                .build());

        if (webhookEnabled) {
            orderClient.postDeliveryStatus(saved.getOrderId(), "DELIVERED", note, jwt).subscribe();
        }

        return saved;
    }

    @Override
    public Shipment fail(Long id,
                         String note,
                         MultipartFile photo,
                         BigDecimal latitude,
                         BigDecimal longitude,
                         Long currentUserId,
                         boolean isAdmin,
                         Jwt jwt) {
        Shipment s = getFor(id, currentUserId, isAdmin);

        String evidenceUrl = storeEvidenceFile(photo);

        s.setStatus(DeliveryStatus.FAILED);
        s.setNote(note);
        s.setEvidenceUrl(evidenceUrl);

        Shipment saved = shipmentRepo.save(s);

        trackingRepo.save(TrackingEvent.builder()
                .shipment(saved)
                .status(DeliveryStatus.FAILED)
                .latitude(latitude)
                .longitude(longitude)
                .note(note)
                .build());

        if (webhookEnabled) {
            orderClient.postDeliveryStatus(saved.getOrderId(), "FAILED", note, jwt).subscribe();
        }

        return saved;
    }

    @Override
    public Shipment cancel(Long id, Long currentUserId, boolean isAdmin) {
        Shipment s = getFor(id, currentUserId, isAdmin);

        if (s.getStatus() == DeliveryStatus.DELIVERED || s.getStatus() == DeliveryStatus.FAILED) {
            throw new IllegalStateException("Cannot cancel a completed shipment");
        }

        s.setStatus(DeliveryStatus.CANCELLED);
        Shipment saved = shipmentRepo.save(s);

        trackingRepo.save(TrackingEvent.builder()
                .shipment(saved)
                .status(DeliveryStatus.CANCELLED)
                .note("Cancelled")
                .build());

        return saved;
    }

    @Override
    public TrackingEvent track(Long id, TrackRequest req, Long currentUserId, boolean isAdmin) {
        Shipment s = getFor(id, currentUserId, isAdmin);

        BigDecimal lat = parseDecimal(req.latitude());
        BigDecimal lng = parseDecimal(req.longitude());

        TrackingEvent te = TrackingEvent.builder()
                .shipment(s)
                .status(req.status() == null ? s.getStatus() : req.status())
                .latitude(lat)
                .longitude(lng)
                .note(req.note())
                .build();

        return trackingRepo.save(te);
    }

private BigDecimal parseDecimal(String v) {
    if (v == null || v.isBlank()) return null;
    try {
        return new BigDecimal(v.trim());
    } catch (Exception e) {
        return null;
    }
}


    @Override
    @Transactional(readOnly = true)
    public List<TrackingEvent> listTracking(Long id, Long currentUserId, boolean isAdmin) {
        Shipment s = getFor(id, currentUserId, isAdmin);
        return trackingRepo.findByShipmentIdOrderByCreatedAtAsc(s.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shipment> listAssignedToMe(Long currentUserId, boolean isAdmin) {
        if (isAdmin) return shipmentRepo.findAll();
        return shipmentRepo.findByAssignedUserIdOrderByUpdatedAtDesc(currentUserId);
    }
}

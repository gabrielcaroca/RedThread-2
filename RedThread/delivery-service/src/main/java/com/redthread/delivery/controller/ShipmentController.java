package com.redthread.delivery.controller;

import com.redthread.delivery.domain.DeliveryStatus;
import com.redthread.delivery.domain.Shipment;
import com.redthread.delivery.domain.ShipmentAssignment;
import com.redthread.delivery.domain.TrackingEvent;
import com.redthread.delivery.dto.shipment.*;
import com.redthread.delivery.security.AuthUtils;
import com.redthread.delivery.service.AssignmentService;
import com.redthread.delivery.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shipments")
public class ShipmentController {

    private final ShipmentService service;
    private final AssignmentService assignmentService;
    private final AuthUtils auth;

    public ShipmentController(ShipmentService service, AssignmentService assignmentService, AuthUtils auth) {
        this.service = service;
        this.assignmentService = assignmentService;
        this.auth = auth;
    }

    @PostMapping
    public ShipmentResponse create(@Valid @RequestBody CreateShipmentRequest req, @AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        Shipment s = service.createFromOrder(req, userId, jwt);
        return map(s);
    }

    @GetMapping
    public List<ShipmentResponse> listMine(@AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        return service.listMine(userId, isAdmin).stream().map(this::map).toList();
    }

    @GetMapping("/{id}")
    public ShipmentResponse get(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        return map(service.getFor(id, userId, isAdmin));
    }

    @PostMapping("/{id}/assign")
    public ShipmentResponse assign(@PathVariable Long id, @Valid @RequestBody AssignRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        assignmentService.assign(id, req.driverId(), req.vehicleId());
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        return map(service.getFor(id, userId, isAdmin));
    }

    @PostMapping("/{id}/start")
    public ShipmentResponse start(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        return map(service.start(id, userId, isAdmin));
    }

    @PostMapping("/{id}/delivered")
    public ShipmentResponse delivered(@PathVariable Long id, @RequestBody(required = false) NoteRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        Shipment s = service.delivered(id, req != null ? req.note() : null, userId, isAdmin, jwt);
        return map(s);
    }

    @PostMapping("/{id}/fail")
    public ShipmentResponse fail(@PathVariable Long id, @Valid @RequestBody NoteRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        Shipment s = service.fail(id, req.note(), userId, isAdmin, jwt);
        return map(s);
    }

    @PostMapping("/{id}/cancel")
    public ShipmentResponse cancel(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        return map(service.cancel(id, userId, isAdmin));
    }

    @PostMapping("/{id}/track")
    public ShipmentResponse track(@PathVariable Long id, @Valid @RequestBody TrackRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        service.track(id, req, userId, isAdmin);
        return map(service.getFor(id, userId, isAdmin));
    }

    @GetMapping("/{id}/tracking")
    public List<Map<String, Object>> tracking(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        List<TrackingEvent> list = service.listTracking(id, userId, isAdmin);
        return list.stream().map(te -> {
            String lat = te.getLatitude() != null ? te.getLatitude().toPlainString() : null;
            String lng = te.getLongitude() != null ? te.getLongitude().toPlainString() : null;
            String st = te.getStatus() != null ? te.getStatus().name() : null;
            return java.util.Map.<String, Object>of(
                    "id", te.getId(),
                    "status", st,
                    "latitude", lat,
                    "longitude", lng,
                    "note", te.getNote(),
                    "createdAt", te.getCreatedAt().toString());
        }).toList();
    }

    private ShipmentResponse map(Shipment s) {
        Long zoneId = s.getZone() != null ? s.getZone().getId() : null;
        return new ShipmentResponse(
                s.getId(), s.getOrderId(), s.getUserId(),
                s.getAddressLine1(), s.getAddressLine2(), s.getCity(), s.getState(), s.getZip(), s.getCountry(),
                zoneId, s.getStatus(), s.getTotalPrice().toPlainString(), s.getCreatedAt(), s.getUpdatedAt());
    }
}

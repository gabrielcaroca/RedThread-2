package com.redthread.delivery.controller;

import com.redthread.delivery.domain.Shipment;
import com.redthread.delivery.domain.TrackingEvent;
import com.redthread.delivery.dto.shipment.*;
import com.redthread.delivery.security.AuthUtils;
import com.redthread.delivery.service.AssignmentService;
import com.redthread.delivery.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService service;
    private final AssignmentService assignmentService;
    private final AuthUtils auth;

    @PostMapping
    public ShipmentResponse create(@Valid @RequestBody CreateShipmentRequest req,
                                   @AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        Shipment s = service.createFromOrder(req, userId, jwt);
        return ShipmentResponse.from(s);
    }

    @GetMapping
    public List<ShipmentResponse> listMine(@AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        return service.listMine(userId, isAdmin).stream().map(ShipmentResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ShipmentResponse get(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        return ShipmentResponse.from(service.getFor(id, userId, isAdmin));
    }

    @PostMapping("/{id}/assign")
    public Map<String, Object> assign(@PathVariable Long id,
                                     @Valid @RequestBody AssignRequest req,
                                     @AuthenticationPrincipal Jwt jwt) {
        if (!auth.hasAdmin(jwt)) throw new SecurityException("Only admin");
        assignmentService.assign(id, req.assignedUserId());
        return Map.of("ok", true);
    }

    @PostMapping("/{id}/start")
    public ShipmentResponse start(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        return ShipmentResponse.from(service.start(id, userId, isAdmin));
    }

    @PostMapping(
            value = "/{id}/delivered",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ShipmentResponse delivered(@PathVariable Long id,
                                      @RequestPart("receiverName") String receiverName,
                                      @RequestPart(value = "note", required = false) String note,
                                      @RequestPart("photo") MultipartFile photo,
                                      @RequestPart(value = "latitude", required = false) BigDecimal latitude,
                                      @RequestPart(value = "longitude", required = false) BigDecimal longitude,
                                      @AuthenticationPrincipal Jwt jwt) {

        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);

        Shipment s = service.delivered(id, receiverName, note, photo, latitude, longitude, userId, isAdmin, jwt);
        return ShipmentResponse.from(s);
    }

    @PostMapping(
            value = "/{id}/fail",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ShipmentResponse fail(@PathVariable Long id,
                                 @RequestPart(value = "note", required = false) String note,
                                 @RequestPart("photo") MultipartFile photo,
                                 @RequestPart(value = "latitude", required = false) BigDecimal latitude,
                                 @RequestPart(value = "longitude", required = false) BigDecimal longitude,
                                 @AuthenticationPrincipal Jwt jwt) {

        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);

        Shipment s = service.fail(id, note, photo, latitude, longitude, userId, isAdmin, jwt);
        return ShipmentResponse.from(s);
    }

    @PostMapping("/{id}/cancel")
    public ShipmentResponse cancel(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        return ShipmentResponse.from(service.cancel(id, userId, isAdmin));
    }

    @PostMapping("/{id}/track")
    public Map<String, Object> track(@PathVariable Long id,
                                     @Valid @RequestBody TrackRequest req,
                                     @AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        TrackingEvent ev = service.track(id, req, userId, isAdmin);
        return Map.of(
                "id", ev.getId(),
                "status", ev.getStatus(),
                "latitude", ev.getLatitude(),
                "longitude", ev.getLongitude(),
                "note", ev.getNote(),
                "createdAt", ev.getCreatedAt()
        );
    }

    @GetMapping("/{id}/tracking")
    public List<Map<String, Object>> tracking(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);

        List<TrackingEvent> list = service.listTracking(id, userId, isAdmin);

        return list.stream()
                .map(ev -> Map.<String, Object>of(
                        "status", ev.getStatus(),
                        "latitude", ev.getLatitude(),
                        "longitude", ev.getLongitude(),
                        "note", ev.getNote(),
                        "createdAt", ev.getCreatedAt()
                ))
                .toList();
    }

    @GetMapping("/assigned-to-me")
    public List<ShipmentResponse> assignedToMe(@AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        return service.listAssignedToMe(userId, isAdmin).stream().map(ShipmentResponse::from).toList();
    }
}

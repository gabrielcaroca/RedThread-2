package com.redthread.delivery.controller;

import com.redthread.delivery.domain.GeoZone;
import com.redthread.delivery.dto.zone.CreateZoneRequest;
import com.redthread.delivery.dto.zone.ZoneResponse;
import com.redthread.delivery.service.ZoneService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/zones")
public class ZoneController {

    private final ZoneService service;

    public ZoneController(ZoneService service) { this.service = service; }

    @PostMapping
    public ZoneResponse create(@Valid @RequestBody CreateZoneRequest req) {
        GeoZone z = service.create(req.name(), req.city(), req.state(), req.country(), req.zipPattern());
        return new ZoneResponse(z.getId(), z.getName(), z.getCity(), z.getState(), z.getCountry(), z.getZipPattern());
    }

    @GetMapping
    public List<ZoneResponse> list() {
        return service.list().stream()
                .map(z -> new ZoneResponse(z.getId(), z.getName(), z.getCity(), z.getState(), z.getCountry(), z.getZipPattern()))
                .toList();
    }
}

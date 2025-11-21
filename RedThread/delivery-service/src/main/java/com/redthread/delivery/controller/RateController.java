package com.redthread.delivery.controller;

import com.redthread.delivery.domain.Rate;
import com.redthread.delivery.dto.rate.CreateRateRequest;
import com.redthread.delivery.dto.rate.RateResponse;
import com.redthread.delivery.service.RateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rates")
public class RateController {

    private final RateService service;

    public RateController(RateService service) { this.service = service; }

    @PostMapping
    public RateResponse create(@Valid @RequestBody CreateRateRequest req) {
        Rate r = service.create(req.zoneId(), req.basePrice(), req.isActive());
        return new RateResponse(r.getId(), r.getZone().getId(),
                r.getBasePrice().toPlainString(),
                r.getPricePerKm() != null ? r.getPricePerKm().toPlainString() : null,
                r.isActive());
    }

    @GetMapping
    public List<RateResponse> list(@RequestParam Long zoneId) {
        return service.listByZone(zoneId).stream()
                .map(r -> new RateResponse(r.getId(), r.getZone().getId(),
                        r.getBasePrice().toPlainString(),
                        r.getPricePerKm() != null ? r.getPricePerKm().toPlainString() : null,
                        r.isActive()))
                .toList();
    }
}

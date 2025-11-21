package com.redthread.delivery.controller;

import com.redthread.delivery.domain.Driver;
import com.redthread.delivery.dto.driver.CreateDriverRequest;
import com.redthread.delivery.dto.driver.DriverResponse;
import com.redthread.delivery.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/drivers")
public class DriverController {

    private final DriverService service;

    public DriverController(DriverService service) { this.service = service; }

    @PostMapping
    public DriverResponse create(@Valid @RequestBody CreateDriverRequest req) {
        Driver d = service.create(req.name(), req.phone(), req.email(), req.active());
        return new DriverResponse(d.getId(), d.getName(), d.getPhone(), d.getEmail(), d.isActive());
    }

    @GetMapping
    public List<DriverResponse> list() {
        return service.list().stream()
                .map(d -> new DriverResponse(d.getId(), d.getName(), d.getPhone(), d.getEmail(), d.isActive()))
                .toList();
    }

    @PatchMapping("/{id}")
    public DriverResponse update(@PathVariable Long id, @RequestBody CreateDriverRequest req) {
        Driver d = service.update(id, req.name(), req.phone(), req.email(), req.active());
        return new DriverResponse(d.getId(), d.getName(), d.getPhone(), d.getEmail(), d.isActive());
    }
}

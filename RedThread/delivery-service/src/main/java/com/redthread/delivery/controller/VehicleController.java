package com.redthread.delivery.controller;

import com.redthread.delivery.domain.Vehicle;
import com.redthread.delivery.dto.vehicle.CreateVehicleRequest;
import com.redthread.delivery.dto.vehicle.VehicleResponse;
import com.redthread.delivery.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService service;

    public VehicleController(VehicleService service) { this.service = service; }

    @PostMapping
    public VehicleResponse create(@Valid @RequestBody CreateVehicleRequest req) {
        Vehicle v = service.create(req.plate(), req.model(), req.capacityKg(), req.active());
        return new VehicleResponse(v.getId(), v.getPlate(), v.getModel(),
                v.getCapacityKg() != null ? v.getCapacityKg().toPlainString() : null, v.isActive());
    }

    @GetMapping
    public List<VehicleResponse> list() {
        return service.list().stream()
                .map(v -> new VehicleResponse(v.getId(), v.getPlate(), v.getModel(),
                        v.getCapacityKg() != null ? v.getCapacityKg().toPlainString() : null, v.isActive()))
                .toList();
    }

    @PatchMapping("/{id}")
    public VehicleResponse update(@PathVariable Long id, @RequestBody CreateVehicleRequest req) {
        Vehicle v = service.update(id, req.plate(), req.model(), req.capacityKg(), req.active());
        return new VehicleResponse(v.getId(), v.getPlate(), v.getModel(),
                v.getCapacityKg() != null ? v.getCapacityKg().toPlainString() : null, v.isActive());
    }
}

package com.redthread.delivery.service.impl;

import com.redthread.delivery.domain.Vehicle;
import com.redthread.delivery.repository.VehicleRepository;
import com.redthread.delivery.service.VehicleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service @Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository repo;

    public VehicleServiceImpl(VehicleRepository repo) { this.repo = repo; }

    @Override
    public Vehicle create(String plate, String model, String capacityKg, Boolean active) {
        Vehicle v = Vehicle.builder()
                .plate(plate).model(model)
                .capacityKg(capacityKg != null && !capacityKg.isBlank() ? new BigDecimal(capacityKg) : null)
                .active(active != null ? active : true)
                .build();
        return repo.save(v);
    }

    @Override @Transactional(readOnly = true)
    public List<Vehicle> list() { return repo.findAll(); }

    @Override
    public Vehicle update(Long id, String plate, String model, String capacityKg, Boolean active) {
        Vehicle v = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
        if (plate != null) v.setPlate(plate);
        if (model != null) v.setModel(model);
        if (capacityKg != null && !capacityKg.isBlank()) v.setCapacityKg(new BigDecimal(capacityKg));
        if (active != null) v.setActive(active);
        return repo.save(v);
    }
}

package com.redthread.delivery.service.impl;

import com.redthread.delivery.domain.Driver;
import com.redthread.delivery.repository.DriverRepository;
import com.redthread.delivery.service.DriverService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @Transactional
public class DriverServiceImpl implements DriverService {

    private final DriverRepository repo;

    public DriverServiceImpl(DriverRepository repo) { this.repo = repo; }

    @Override
    public Driver create(String name, String phone, String email, Boolean active) {
        Driver d = Driver.builder()
                .name(name).phone(phone).email(email)
                .active(active != null ? active : true)
                .build();
        return repo.save(d);
    }

    @Override @Transactional(readOnly = true)
    public List<Driver> list() { return repo.findAll(); }

    @Override
    public Driver update(Long id, String name, String phone, String email, Boolean active) {
        Driver d = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Driver not found"));
        if (name != null) d.setName(name);
        if (phone != null) d.setPhone(phone);
        if (email != null) d.setEmail(email);
        if (active != null) d.setActive(active);
        return repo.save(d);
    }
}

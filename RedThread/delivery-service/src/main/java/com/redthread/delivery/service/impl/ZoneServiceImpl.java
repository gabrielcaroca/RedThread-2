package com.redthread.delivery.service.impl;

import com.redthread.delivery.domain.GeoZone;
import com.redthread.delivery.repository.GeoZoneRepository;
import com.redthread.delivery.service.ZoneService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @Transactional
public class ZoneServiceImpl implements ZoneService {

    private final GeoZoneRepository repo;

    public ZoneServiceImpl(GeoZoneRepository repo) { this.repo = repo; }

    @Override
    public GeoZone create(String name, String city, String state, String country, String zipPattern) {
        GeoZone z = GeoZone.builder()
                .name(name).city(city).state(state).country(country).zipPattern(zipPattern)
                .build();
        return repo.save(z);
    }

    @Override @Transactional(readOnly = true)
    public List<GeoZone> list() { return repo.findAll(); }
}

package com.redthread.delivery.service.impl;

import com.redthread.delivery.domain.GeoZone;
import com.redthread.delivery.domain.Rate;
import com.redthread.delivery.repository.GeoZoneRepository;
import com.redthread.delivery.repository.RateRepository;
import com.redthread.delivery.service.RateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service @Transactional
public class RateServiceImpl implements RateService {

    private final RateRepository rateRepo;
    private final GeoZoneRepository zoneRepo;

    public RateServiceImpl(RateRepository rateRepo, GeoZoneRepository zoneRepo) {
        this.rateRepo = rateRepo;
        this.zoneRepo = zoneRepo;
    }

    @Override
    public Rate create(Long zoneId, Long basePrice, Boolean isActive) {
        GeoZone zone = zoneRepo.findById(zoneId).orElseThrow(() -> new IllegalArgumentException("Zone not found"));
        Rate r = Rate.builder()
                .zone(zone)
                .basePrice(new BigDecimal(basePrice))
                .isActive(isActive != null ? isActive : true)
                .build();
        return rateRepo.save(r);
    }

    @Override @Transactional(readOnly = true)
    public List<Rate> listByZone(Long zoneId) {
        return rateRepo.findByZoneIdAndIsActiveTrue(zoneId);
    }
}

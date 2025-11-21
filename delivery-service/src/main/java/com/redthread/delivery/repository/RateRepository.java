package com.redthread.delivery.repository;

import com.redthread.delivery.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RateRepository extends JpaRepository<Rate, Long> {
    List<Rate> findByZoneIdAndIsActiveTrue(Long zoneId);
}

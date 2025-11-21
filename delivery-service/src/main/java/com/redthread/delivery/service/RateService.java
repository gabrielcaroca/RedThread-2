package com.redthread.delivery.service;

import com.redthread.delivery.domain.*;
import java.util.List;

public interface RateService {
    Rate create(Long zoneId, Long basePrice, Boolean isActive);
    List<Rate> listByZone(Long zoneId);
}

package com.redthread.delivery.service;

import com.redthread.delivery.domain.*;
import java.util.List;

public interface VehicleService {
    Vehicle create(String plate, String model, String capacityKg, Boolean active);
    List<Vehicle> list();
    Vehicle update(Long id, String plate, String model, String capacityKg, Boolean active);
}
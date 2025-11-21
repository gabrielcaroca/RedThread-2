package com.redthread.delivery.service;

import com.redthread.delivery.domain.*;

public interface AssignmentService {
    ShipmentAssignment assign(Long shipmentId, Long driverId, Long vehicleId);
}

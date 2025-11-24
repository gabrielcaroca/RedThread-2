package com.redthread.delivery.service;

import com.redthread.delivery.domain.ShipmentAssignment;

public interface AssignmentService {
    ShipmentAssignment assign(Long shipmentId, Long assignedUserId);
}

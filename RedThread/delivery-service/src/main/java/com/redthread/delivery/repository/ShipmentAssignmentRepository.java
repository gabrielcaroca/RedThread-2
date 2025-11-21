package com.redthread.delivery.repository;

import com.redthread.delivery.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShipmentAssignmentRepository extends JpaRepository<ShipmentAssignment, Long> {
    List<ShipmentAssignment> findByShipmentId(Long shipmentId);
}

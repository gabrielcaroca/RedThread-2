package com.redthread.delivery.repository;

import com.redthread.delivery.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    List<Shipment> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Shipment> findByRouteId(Long routeId);
    List<Shipment> findByAssignedUserIdOrderByUpdatedAtDesc(Long assignedUserId);
}

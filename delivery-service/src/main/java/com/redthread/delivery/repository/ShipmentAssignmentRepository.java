package com.redthread.delivery.repository;

import com.redthread.delivery.domain.ShipmentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentAssignmentRepository extends JpaRepository<ShipmentAssignment, Long> {

    List<ShipmentAssignment> findByShipmentId(Long shipmentId);

    boolean existsByAssignedUserIdAndShipmentId(Long assignedUserId, Long shipmentId);

    List<ShipmentAssignment> findByAssignedUserIdOrderByAssignedAtDesc(Long assignedUserId);

    Optional<ShipmentAssignment> findFirstByShipmentIdOrderByAssignedAtDesc(Long shipmentId);
}

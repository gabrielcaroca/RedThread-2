package com.redthread.delivery.service.impl;

import com.redthread.delivery.domain.DeliveryStatus;
import com.redthread.delivery.domain.Shipment;
import com.redthread.delivery.domain.ShipmentAssignment;
import com.redthread.delivery.repository.ShipmentAssignmentRepository;
import com.redthread.delivery.repository.ShipmentRepository;
import com.redthread.delivery.service.AssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class AssignmentServiceImpl implements AssignmentService {

    private final ShipmentRepository shipmentRepo;
    private final ShipmentAssignmentRepository assignmentRepo;

    public AssignmentServiceImpl(
            ShipmentRepository shipmentRepo,
            ShipmentAssignmentRepository assignmentRepo
    ) {
        this.shipmentRepo = shipmentRepo;
        this.assignmentRepo = assignmentRepo;
    }

    @Override
    public ShipmentAssignment assign(Long shipmentId, Long assignedUserId) {
        Shipment s = shipmentRepo.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found"));

        s.setStatus(DeliveryStatus.ASSIGNED);
        shipmentRepo.save(s);

        ShipmentAssignment a = ShipmentAssignment.builder()
                .shipment(s)
                .assignedUserId(assignedUserId)
                .assignedAt(Instant.now())
                .build();

        return assignmentRepo.save(a);
    }
}

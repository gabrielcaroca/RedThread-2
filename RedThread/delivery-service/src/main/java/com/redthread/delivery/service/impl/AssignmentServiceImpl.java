package com.redthread.delivery.service.impl;

import com.redthread.delivery.domain.*;
import com.redthread.delivery.repository.*;
import com.redthread.delivery.service.AssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class AssignmentServiceImpl implements AssignmentService {

    private final ShipmentRepository shipmentRepo;
    private final DriverRepository driverRepo;
    private final VehicleRepository vehicleRepo;
    private final ShipmentAssignmentRepository assignmentRepo;

    public AssignmentServiceImpl(ShipmentRepository shipmentRepo, DriverRepository driverRepo,
                                 VehicleRepository vehicleRepo, ShipmentAssignmentRepository assignmentRepo) {
        this.shipmentRepo = shipmentRepo;
        this.driverRepo = driverRepo;
        this.vehicleRepo = vehicleRepo;
        this.assignmentRepo = assignmentRepo;
    }

    @Override
    public ShipmentAssignment assign(Long shipmentId, Long driverId, Long vehicleId) {
        Shipment s = shipmentRepo.findById(shipmentId).orElseThrow(() -> new IllegalArgumentException("Shipment not found"));
        Driver d = driverRepo.findById(driverId).orElseThrow(() -> new IllegalArgumentException("Driver not found"));
        Vehicle v = null;
        if (vehicleId != null) v = vehicleRepo.findById(vehicleId).orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        s.setStatus(DeliveryStatus.ASSIGNED);
        shipmentRepo.save(s);

        ShipmentAssignment a = ShipmentAssignment.builder().shipment(s).driver(d).vehicle(v).build();
        return assignmentRepo.save(a);
    }
}

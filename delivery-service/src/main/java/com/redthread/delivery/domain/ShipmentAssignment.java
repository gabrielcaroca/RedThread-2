package com.redthread.delivery.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity @Table(name = "shipment_assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShipmentAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="shipment_id")
    private Shipment shipment;

    @ManyToOne(optional=false) @JoinColumn(name="driver_id")
    private Driver driver;

    @ManyToOne @JoinColumn(name="vehicle_id")
    private Vehicle vehicle;

    @Column(nullable=false)
    private Instant assignedAt;

    @PrePersist
    public void prePersist() { this.assignedAt = Instant.now(); }
}

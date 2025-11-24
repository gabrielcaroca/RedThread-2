package com.redthread.delivery.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "shipment_assignments")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    // userId del despachador en identity-service
    @Column(name = "assigned_user_id", nullable = false)
    private Long assignedUserId;

    @Column(name="assigned_at", nullable = false, updatable=false)
    private Instant assignedAt;

    @PrePersist
    public void prePersist() {
        if (this.assignedAt == null) this.assignedAt = Instant.now();
    }
}

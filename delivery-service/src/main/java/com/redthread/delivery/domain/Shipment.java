package com.redthread.delivery.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "shipments")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="order_id", nullable=false)
    private Long orderId;

    // dueño del pedido (cliente)
    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="address_line1", nullable=false, length=200)
    private String addressLine1;

    @Column(name="address_line2", length=200)
    private String addressLine2;

    @Column(nullable=false, length=120)
    private String city;

    @Column(length=120)
    private String state;

    @Column(length=40)
    private String zip;

    @Column(nullable=false, length=80)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=32)
    private DeliveryStatus status = DeliveryStatus.PENDING_PICKUP;

    // despachador asignado (por ruta tomada o asignación directa)
    @Column(name="assigned_user_id")
    private Long assignedUserId;

    // precio fijo por pedido
    @Column(name="total_price", nullable=false, precision=12, scale=2)
    private BigDecimal totalPrice;

    // evidencia (entrega o fallo)
    @Column(name="evidence_url", length=300)
    private String evidenceUrl;

    @Column(name="receiver_name", length=120)
    private String receiverName;

    @Column(length=500)
    private String note;

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt;

    @Column(name="updated_at", nullable=false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private DeliveryRoute route;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) this.status = DeliveryStatus.PENDING_PICKUP;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}

package com.redthread.delivery.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name = "shipments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Shipment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long orderId;

    @Column(nullable=false)
    private Long userId;

    @Column(nullable=false, length=200)
    private String addressLine1;
    @Column(length=200)
    private String addressLine2;
    @Column(nullable=false, length=120)
    private String city;
    private String state;
    private String zip;
    @Column(nullable=false, length=80)
    private String country;

    @ManyToOne
    @JoinColumn(name="zone_id")
    private GeoZone zone;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=32)
    private DeliveryStatus status;

    @Column(nullable=false, precision=12, scale=2)
    private BigDecimal totalPrice;

    @Column(nullable=false)
    private Instant createdAt;
    @Column(nullable=false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}

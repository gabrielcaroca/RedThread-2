package com.redthread.delivery.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name = "tracking_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrackingEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="shipment_id")
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(length=32)
    private DeliveryStatus status;

    @Column(precision=10, scale=6)
    private BigDecimal latitude;

    @Column(precision=10, scale=6)
    private BigDecimal longitude;

    @Column(length=250)
    private String note;

    @Column(nullable=false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() { this.createdAt = Instant.now(); }
}

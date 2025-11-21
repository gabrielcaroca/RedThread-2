package com.redthread.delivery.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity @Table(name = "rates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="zone_id")
    private GeoZone zone;

    @Column(nullable=false, precision=12, scale=2)
    private BigDecimal basePrice;

    @Column(precision=12, scale=2)
    private BigDecimal pricePerKm;

    @Column(nullable=false)
    private boolean isActive = true;
}

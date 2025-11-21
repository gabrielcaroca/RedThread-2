package com.redthread.delivery.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity @Table(name = "vehicles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vehicle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true, length=32)
    private String plate;
    @Column(length=120)
    private String model;
    private BigDecimal capacityKg;
    @Column(nullable=false)
    private boolean active = true;
}

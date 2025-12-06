package com.redthread.catalog.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties("variant") // evitamos recursión al serializar
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "variant_id", unique = true)
    private Variant variant;

    @Column(name = "stock_available", nullable = false)
    private int stockAvailable;

    @Column(name = "stock_reserved", nullable = false)
    private int stockReserved;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}

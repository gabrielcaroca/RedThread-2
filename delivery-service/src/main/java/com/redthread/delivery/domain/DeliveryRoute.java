package com.redthread.delivery.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "delivery_routes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=120)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable=false)
    private Integer totalPedidos;

    // total estimado de la ruta completa
    @Column(nullable=false)
    private Long totalPrice; // CLP

    @Column(nullable=false)
    private Boolean activa = true;

    // null hasta que un despachador la toma
    private Long assignedUserId;

    @Column(nullable=false, updatable=false)
    private Instant createdAt;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<Shipment> shipments = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = Instant.now();
        if (this.activa == null) this.activa = true;
    }
}

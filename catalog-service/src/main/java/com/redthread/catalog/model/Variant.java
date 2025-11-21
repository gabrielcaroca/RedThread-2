package com.redthread.catalog.model;

import com.redthread.catalog.model.enums.SizeType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name = "variants",
    uniqueConstraints = {
        @UniqueConstraint(name="uq_variant", columnNames = {"product_id","size_type","size_value","color"})
    })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Variant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name="size_type", nullable=false, length=16)
    private SizeType sizeType;

    @Column(name="size_value", nullable=false, length=16)
    private String sizeValue; // EU: "39".."46" | LETTER: "XS".."XXL"

    @Column(nullable=false, length=40)
    private String color;

    @Column(nullable=false, unique=true, length=64)
    private String sku;

    @Column(name="price_override", precision=12, scale=2)
    private BigDecimal priceOverride;

    @Column(nullable=false)
    private boolean active = true;

    @Column(name="created_at", nullable=false)
    private Instant createdAt = Instant.now();
}

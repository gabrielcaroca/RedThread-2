package com.redthread.catalog.model;

import com.redthread.catalog.model.enums.SizeType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "variants",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_variant",
            columnNames = {"product_id", "size_type", "size_value", "color"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Variant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Producto padre
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    // Tipo de talla (EU, LETTER, etc.)
    @Enumerated(EnumType.STRING)
    @Column(name = "size_type", nullable = false, length = 16)
    private SizeType sizeType;

    // Valor de la talla (42, M, L, etc.)
    @Column(name = "size_value", nullable = false, length = 16)
    private String sizeValue; // EU: "39".."46" | LETTER: "XS".."XXL"

    // Color de la variante
    @Column(nullable = false, length = 40)
    private String color;

    // SKU único
    @Column(nullable = false, unique = true, length = 64)
    private String sku;

    // Precio específico de la variante (si no es null, sobreescribe el basePrice del producto)
    @Column(name = "price_override", precision = 12, scale = 2)
    private BigDecimal priceOverride;

    // 🔹 Relación 1:1 con Inventory para exponer stock en JSON
    @OneToOne(mappedBy = "variant", fetch = FetchType.LAZY)
    private Inventory inventory;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}

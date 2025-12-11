package com.redthread.catalog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Variant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Producto padre (no lo exponemos entero en JSON para evitar problemas de lazy)
    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
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

    // Inventario 1:1 (lo ocultamos en JSON y exponemos solo el stock calculado)
    @JsonIgnore
    @OneToOne(mappedBy = "variant", fetch = FetchType.LAZY)
    private Inventory inventory;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    // ==========================
    // Campos "virtuales" para JSON
    // ==========================

    // productId plano para que encaje con tu VariantDto
    @Transient
    @JsonProperty("productId")
    public Long getProductIdForJson() {
        return (product != null) ? product.getId() : null;
    }

    // stock calculado a partir del inventario
    @Transient
    @JsonProperty("stock")
    public Integer getStockForJson() {
        if (inventory == null) {
            return null; // la app ya hace ?: 0 si viene null
        }
        int disponible = inventory.getStockAvailable() - inventory.getStockReserved();
        return Math.max(disponible, 0);
    }
}

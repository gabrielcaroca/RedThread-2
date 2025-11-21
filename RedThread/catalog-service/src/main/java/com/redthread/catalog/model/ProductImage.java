package com.redthread.catalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity @Table(name = "product_images")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="product_id")
    private Product product;

    @Column(name="file_path", nullable=false, columnDefinition="text")
    private String filePath; // ruta local absoluta o relativa

    @Column(name="public_url", nullable=false, columnDefinition="text")
    private String publicUrl; // /media/...

    @Column(name="is_primary", nullable=false)
    private boolean primary;

    @Column(name="sort_order", nullable=false)
    private int sortOrder;

    @Column(name="created_at", nullable=false)
    private Instant createdAt = Instant.now();
}

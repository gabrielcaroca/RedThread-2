package com.redthread.catalog.controller.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(name = "ImageDto", description = "Imagen asociada a un producto")
public record ImageDto(
        @Schema(example = "3")
        Long id,

        @Schema(example = "10")
        Long productId,

        @Schema(example = "/media/products/10/img1.jpg")
        String publicUrl,

        @Schema(example = "true")
        boolean primary,

        @Schema(example = "0")
        int sortOrder,
        
        @Schema(example = "2024-10-01T12:30:00Z")
        Instant createdAt
) {}

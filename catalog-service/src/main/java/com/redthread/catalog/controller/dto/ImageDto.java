package com.redthread.catalog.controller.dto;

import java.time.Instant;

public record ImageDto(
        Long id,
        Long productId,
        String publicUrl,
        boolean primary,
        int sortOrder,
        Instant createdAt
) {}

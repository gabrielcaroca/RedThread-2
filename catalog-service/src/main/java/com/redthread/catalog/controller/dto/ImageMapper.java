package com.redthread.catalog.controller.dto;

import com.redthread.catalog.model.ProductImage;

public class ImageMapper {

    public static ImageDto toDto(ProductImage img) {
        return new ImageDto(
                img.getId(),
                img.getProduct().getId(),
                img.getPublicUrl(),
                img.isPrimary(),
                img.getSortOrder(),
                img.getCreatedAt()
        );
    }
}

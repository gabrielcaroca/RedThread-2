package com.redthread.order.dto;

public record CatalogVariantRes(
        Long variantId,
        String productName,
        String size,
        String color
) {}

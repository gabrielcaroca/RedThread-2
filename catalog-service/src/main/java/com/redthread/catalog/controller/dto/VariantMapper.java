package com.redthread.catalog.controller.dto;

import com.redthread.catalog.model.Inventory;
import com.redthread.catalog.model.Variant;

public class VariantMapper {

    public static VariantDto toDto(Variant v) {

        Integer stock = null;
        Inventory inv = v.getInventory();

        if (inv != null) {
            stock = inv.getStockAvailable();
        }

        return new VariantDto(
                v.getId(),
                v.getProduct().getId(),
                v.getSizeType().name(),
                v.getSizeValue(),
                v.getColor(),
                v.getSku(),
                v.getPriceOverride(),
                stock
        );
    }
}

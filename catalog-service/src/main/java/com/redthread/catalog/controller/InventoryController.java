package com.redthread.catalog.controller;

import com.redthread.catalog.controller.dto.AdjustStockReq;
import com.redthread.catalog.model.Inventory;
import com.redthread.catalog.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService service;

    @GetMapping("/by-variant/{variantId}")
    public Inventory byVariant(@PathVariable Long variantId) {
        return service.getByVariant(variantId);
    }

    @PostMapping("/adjust")
    public Inventory adjust(@RequestBody @Valid AdjustStockReq req) {
        return service.adjustStock(req.variantId(), req.delta());
    }
}

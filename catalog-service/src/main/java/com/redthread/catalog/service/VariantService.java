package com.redthread.catalog.service;

import com.redthread.catalog.controller.dto.CreateVariantReq;
import com.redthread.catalog.model.Inventory;
import com.redthread.catalog.model.Product;
import com.redthread.catalog.model.Variant;
import com.redthread.catalog.model.enums.SizeType;
import com.redthread.catalog.repository.InventoryRepository;
import com.redthread.catalog.repository.ProductRepository;
import com.redthread.catalog.repository.VariantRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VariantService {

    private final VariantRepository variantRepo;
    private final ProductRepository productRepo;
    private final InventoryRepository inventoryRepo;

    // ----------------------------------------
    // CREATE VARIANT + STOCK
    // ----------------------------------------
    public Variant create(CreateVariantReq req) {

        Product product = productRepo.findById(req.productId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        SizeValidator.validate(req.sizeType(), req.sizeValue());

        boolean exists = variantRepo.existsByProductIdAndSizeTypeAndSizeValueAndColor(
                req.productId(),
                req.sizeType(),
                req.sizeValue().toUpperCase(),
                req.color().toUpperCase()
        );

        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Variante duplicada para producto/talla/color");
        }

        String finalSku = (req.sku() == null || req.sku().isBlank())
                ? "SKU-" + UUID.randomUUID()
                : req.sku().trim();

        Variant v = Variant.builder()
                .product(product)
                .sizeType(req.sizeType())
                .sizeValue(req.sizeValue().toUpperCase())
                .color(req.color().toUpperCase())
                .sku(finalSku)
                .priceOverride(req.priceOverride())
                .active(true)
                .createdAt(java.time.Instant.now())
                .build();

        Variant saved = variantRepo.save(v);

        // ----------------------------------------
        // CREAR INVENTARIO CON STOCK INICIAL
        // ----------------------------------------
        int initialStock = req.stock() != null ? req.stock() : 0;

        inventoryRepo.save(
                Inventory.builder()
                        .variant(saved)
                        .stockAvailable(initialStock)
                        .stockReserved(0)
                        .updatedAt(java.time.Instant.now())
                        .build()
        );

        return saved;
    }

    // ----------------------------------------
    public Variant get(Long id) {
        return variantRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Variante no existe"));
    }

    public List<Variant> byProduct(Long productId) {
        return variantRepo.findByProductId(productId);
    }

    public Variant update(Long id, CreateVariantReq req) {

        Variant existing = variantRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variante no encontrada"));

        boolean exists = variantRepo.existsByProductIdAndSizeTypeAndSizeValueAndColor(
                req.productId(), req.sizeType(), req.sizeValue(), req.color());

        if (exists && !existing.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Otra variante con esa combinación ya existe");
        }

        existing.setSizeType(req.sizeType());
        existing.setSizeValue(req.sizeValue());
        existing.setColor(req.color());
        existing.setSku(req.sku());
        existing.setPriceOverride(req.priceOverride());

        return variantRepo.save(existing);
    }
}

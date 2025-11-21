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

    public Variant create(Long productId, SizeType sizeType, String sizeValue, String color, String sku,
            BigDecimal priceOverride) {

        System.out.println("Producto recibido en VariantService: " + productId);

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        SizeValidator.validate(sizeType, sizeValue);

        boolean exists = variantRepo.existsByProductIdAndSizeTypeAndSizeValueAndColor(
                productId, sizeType, sizeValue.toUpperCase(), color.toUpperCase());

        if (exists) {
            System.out.println("Variante duplicada detectada para el producto " + productId
                    + " con talla " + sizeValue + " y color " + color);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Variante duplicada para producto/talla/color");
        }

        String finalSku = (sku == null || sku.isBlank()) ? "SKU-" + UUID.randomUUID() : sku.trim();

        Variant v = Variant.builder()
                .product(product)
                .sizeType(sizeType)
                .sizeValue(sizeValue.toUpperCase())
                .color(color.toUpperCase())
                .sku(finalSku)
                .priceOverride(priceOverride)
                .active(true)
                .createdAt(java.time.Instant.now())
                .build();

        Variant saved = variantRepo.save(v);

        inventoryRepo.findByVariantId(saved.getId())
        .orElseGet(() -> inventoryRepo.save(
                Inventory.builder()
                        .variant(saved)
                        .stockAvailable(0)
                        .stockReserved(0)
                        .updatedAt(java.time.Instant.now()) 
                        .build()
        ));


        return saved;
    }

    public Variant get(Long id) {
        return variantRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Variante no existe"));
    }

    public List<Variant> byProduct(Long productId) {
        return variantRepo.findByProductId(productId);
    }

    public Variant update(Long id, CreateVariantReq req) {
        Variant existing = variantRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variante no encontrada"));

        // Validar duplicado solo si cambian combinaciones
        boolean exists = variantRepo.existsByProductIdAndSizeTypeAndSizeValueAndColor(
                req.productId(), req.sizeType(), req.sizeValue(), req.color());

        if (exists && !existing.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Otra variante con esa combinación ya existe");
        }

        existing.setSizeType(req.sizeType());
        existing.setSizeValue(req.sizeValue());
        existing.setColor(req.color());
        existing.setSku(req.sku());
        existing.setPriceOverride(req.priceOverride());

        return variantRepo.save(existing);
    }

}

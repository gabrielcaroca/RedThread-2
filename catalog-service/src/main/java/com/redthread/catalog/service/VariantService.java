package com.redthread.catalog.service;

import com.redthread.catalog.controller.dto.CreateVariantReq;
import com.redthread.catalog.model.Inventory;
import com.redthread.catalog.model.Product;
import com.redthread.catalog.model.Variant;
import com.redthread.catalog.repository.InventoryRepository;
import com.redthread.catalog.repository.ProductRepository;
import com.redthread.catalog.repository.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VariantService {

    private final VariantRepository variantRepo;
    private final ProductRepository productRepo;
    private final InventoryRepository inventoryRepo;

    // =========================
    // Crear variante
    // =========================
    public Variant create(CreateVariantReq req) {
        try {
            // 1) Validar producto
            Product product = productRepo.findById(req.productId())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

            // 2) Validar talla (usa SizeValidator)
            try {
                SizeValidator.validate(req.sizeType(), req.sizeValue());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
            }

            String sizeValueUp = req.sizeValue().toUpperCase();
            String colorUp = req.color().toUpperCase();

            // 3) Evitar duplicados por combinación (producto + talla + color)
            boolean exists = variantRepo.existsByProductIdAndSizeTypeAndSizeValueAndColor(
                    product.getId(),
                    req.sizeType(),
                    sizeValueUp,
                    colorUp
            );
            if (exists) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Ya existe una variante con esa talla y color para este producto"
                );
            }

            // 4) SKU (si viene vacío generamos uno simple)
            String sku = req.sku();
            if (sku == null || sku.isBlank()) {
                sku = "SKU-" + product.getId()
                        + "-" + req.sizeType()
                        + "-" + sizeValueUp
                        + "-" + colorUp;
            }

            // Verificar que el SKU no esté repetido en otra variante
            variantRepo.findBySku(sku).ifPresent(v -> {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El SKU ya está en uso por otra variante"
                );
            });

            // 5) Crear variante
            Variant variant = Variant.builder()
                    .product(product)
                    .sizeType(req.sizeType())
                    .sizeValue(sizeValueUp)
                    .color(colorUp)
                    .sku(sku)
                    .active(true)
                    .createdAt(Instant.now())
                    .build();

            Variant saved = variantRepo.save(variant);

            // 6) Crear inventario asociado
            int stockInitial = req.stock() != null ? req.stock() : 0;

            Inventory inventory = Inventory.builder()
                    .variant(saved)
                    .stockAvailable(stockInitial)
                    .stockReserved(0)
                    .updatedAt(Instant.now())
                    .build();

            inventoryRepo.save(inventory);

            return saved;
        } catch (ResponseStatusException ex) {
            // Ya viene con código y mensaje correcto
            throw ex;
        } catch (DataIntegrityViolationException ex) {
            // Algo chocó con constraints de BD (únicos, etc.)
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pudo guardar la variante (verifica SKU y combinación de talla/color)"
            );
        } catch (Exception ex) {
            // Cualquier otro error inesperado → 500 pero con mensaje claro
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error interno al crear la variante"
            );
        }
    }

    // =========================
    // Obtener una variante (core)
    // =========================
    public Variant getOrThrow(Long id) {
        return variantRepo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Variante no encontrada"));
    }

    // =========================
    // Listar por producto (core)
    // =========================
    public List<Variant> listByProduct(Long productId) {
        if (!productRepo.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        return variantRepo.findByProductId(productId);
    }

    // =========================
    // Actualizar variante
    // =========================
    public Variant update(Long id, CreateVariantReq req) {
        try {
            Variant existing = variantRepo.findById(id)
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Variante no existe"));

            Product product = productRepo.findById(req.productId())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

            try {
                SizeValidator.validate(req.sizeType(), req.sizeValue());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
            }

            String sizeValueUp = req.sizeValue().toUpperCase();
            String colorUp = req.color().toUpperCase();

            boolean exists = variantRepo.existsByProductIdAndSizeTypeAndSizeValueAndColor(
                    product.getId(),
                    req.sizeType(),
                    sizeValueUp,
                    colorUp
            );

            if (exists && !existing.getId().equals(id)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Otra variante con esa combinación ya existe"
                );
            }

            existing.setProduct(product);
            existing.setSizeType(req.sizeType());
            existing.setSizeValue(sizeValueUp);
            existing.setColor(colorUp);

            String sku = req.sku();
            if (sku == null || sku.isBlank()) {
                // Si no mandan SKU en el update, mantenemos el actual
                sku = existing.getSku();
            }
            existing.setSku(sku);

            return variantRepo.save(existing);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pudo actualizar la variante (posible conflicto de SKU)"
            );
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error interno al actualizar la variante"
            );
        }
    }

    // =========================
    // Eliminar variante
    // =========================
    public void delete(Long id) {
        Variant variant = variantRepo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Variante no existe"));

        inventoryRepo.findByVariantId(id).ifPresent(inventoryRepo::delete);
        variantRepo.delete(variant);
    }

    // =========================
    // Métodos de compatibilidad con VariantController
    // =========================

    // El controller llama a service.get(id)
    public Variant get(Long id) {
        return getOrThrow(id);
    }

    // El controller llama a service.byProduct(productId)
    public List<Variant> byProduct(Long productId) {
        return listByProduct(productId);
    }
}

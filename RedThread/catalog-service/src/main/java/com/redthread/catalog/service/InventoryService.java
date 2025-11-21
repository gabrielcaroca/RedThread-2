package com.redthread.catalog.service;

import com.redthread.catalog.model.Inventory;
import com.redthread.catalog.model.Variant;
import com.redthread.catalog.repository.InventoryRepository;
import com.redthread.catalog.repository.VariantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository repo;
    private final VariantRepository variantRepo;

    public Inventory getByVariant(Long variantId) {
        return repo.findByVariantId(variantId)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no existe"));
    }

    @Transactional
    public Inventory adjustStock(Long variantId, int delta) {
        Variant variant = variantRepo.findById(variantId)
                .orElseThrow(() -> new EntityNotFoundException("Variante no existe"));

        Inventory inv = repo.findByVariantId(variantId)
                .orElseGet(() -> Inventory.builder()
                        .variant(variant)
                        .stockAvailable(0)
                        .stockReserved(0)
                        .updatedAt(Instant.now()) 
                        .build()
                );

        long next = (long) inv.getStockAvailable() + delta;

        if (next < 0)
            throw new IllegalArgumentException("No puedes dejar stock negativo");
        if (next > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Stock fuera de rango");

        inv.setStockAvailable((int) next);
        inv.setUpdatedAt(Instant.now()); 

        return repo.save(inv);
    }
    
}

package com.redthread.catalog.service;

import com.redthread.catalog.model.Inventory;
import com.redthread.catalog.model.Variant;
import com.redthread.catalog.repository.InventoryRepository;
import com.redthread.catalog.repository.VariantRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InventoryServiceTest {

    InventoryRepository repo = mock(InventoryRepository.class);
    VariantRepository variantRepo = mock(VariantRepository.class);

    InventoryService service =
            new InventoryService(repo, variantRepo);

    @Test
    void adjustStock_negativeResult_throwsException() {

        // Arrange
        Variant v = Variant.builder()
                .id(1L)
                .build();

        Inventory inventory = Inventory.builder()
                .variant(v)
                .stockAvailable(0)
                .stockReserved(0)
                .build();

        when(variantRepo.findById(1L)).thenReturn(Optional.of(v));
        when(repo.findByVariantId(1L)).thenReturn(Optional.of(inventory));

        // Act + Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.adjustStock(1L, -5)
        );

        assertEquals("No puedes dejar stock negativo", ex.getMessage());
    }
}

package com.redthread.catalog.service;

import com.redthread.catalog.controller.dto.CreateVariantReq;
import com.redthread.catalog.model.enums.SizeType;
import com.redthread.catalog.repository.InventoryRepository;
import com.redthread.catalog.repository.ProductRepository;
import com.redthread.catalog.repository.VariantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VariantServiceTest {

    VariantRepository variantRepo = mock(VariantRepository.class);
    ProductRepository productRepo = mock(ProductRepository.class);
    InventoryRepository inventoryRepo = mock(InventoryRepository.class);

    VariantService service = new VariantService(variantRepo, productRepo, inventoryRepo);

    @Test
    void create_productNotFound_throws404() {
        when(productRepo.findById(1L)).thenReturn(Optional.empty());

        CreateVariantReq req = new CreateVariantReq(
                1L,
                SizeType.LETTER,
                "M",
                "NEGRO",
                null,
                1);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.create(req));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}

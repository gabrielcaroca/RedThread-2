package com.redthread.catalog.service;

import com.redthread.catalog.model.Brand;
import com.redthread.catalog.model.Category;
import com.redthread.catalog.model.Product;
import com.redthread.catalog.model.enums.ProductGender;
import com.redthread.catalog.repository.BrandRepository;
import com.redthread.catalog.repository.CategoryRepository;
import com.redthread.catalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductRepository productRepo;
    private CategoryRepository categoryRepo;
    private BrandRepository brandRepo;
    private ProductService service;

    @BeforeEach
    void setup() {
        productRepo = mock(ProductRepository.class);
        categoryRepo = mock(CategoryRepository.class);
        brandRepo = mock(BrandRepository.class);
        service = new ProductService(productRepo, categoryRepo, brandRepo);
    }

    @Test
    void create_validProduct_saves() {
        Category cat = Category.builder().id(1L).name("Poleras").build();
        Brand brand = Brand.builder().id(2L).name("Nike").build();

        when(categoryRepo.findById(1L)).thenReturn(Optional.of(cat));
        when(brandRepo.findById(2L)).thenReturn(Optional.of(brand));
        when(productRepo.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product p = service.create(
                1L, 2L, "Polera Negra", "desc",
                new BigDecimal("12990.00"),
                true,
                ProductGender.HOMBRE
        );

        assertNotNull(p);
        assertTrue(p.isFeatured());
        assertEquals(ProductGender.HOMBRE, p.getGender());
    }

    @Test
    void create_negativePrice_throws400() {
        Category cat = Category.builder().id(1L).name("Poleras").build();
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(cat));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(1L, null, "X", null,
                        new BigDecimal("-1"),
                        false,
                        ProductGender.MUJER));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}

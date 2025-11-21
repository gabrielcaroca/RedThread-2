package com.redthread.catalog.service;

import com.redthread.catalog.model.*;
import com.redthread.catalog.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final BrandRepository brandRepo;

    public Product create(Long categoryId, Long brandId, String name, String description, BigDecimal basePrice) {
        Category cat = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no existe"));
        Brand brand = null;
        if (brandId != null) {
            brand = brandRepo.findById(brandId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no existe"));
        }
        if (basePrice == null || basePrice.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precio base inválido");
        }

        Product p = Product.builder()
                .category(cat)
                .brand(brand)
                .name(name.trim())
                .description(description)
                .basePrice(basePrice)
                .active(true)
                .build();

        return productRepo.save(p);
    }

    public Product get(Long id) {
        return productRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Producto no existe"));
    }

    public List<Product> byCategory(Long categoryId) {
        return productRepo.findByCategoryId(categoryId);
    }

    public Product update(Long id, Long categoryId, Long brandId, String name, String description, BigDecimal basePrice) {
    Product existing = productRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no existe"));

    Category cat = categoryRepo.findById(categoryId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no existe"));

    Brand brand = null;
    if (brandId != null) {
        brand = brandRepo.findById(brandId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no existe"));
    }

    if (basePrice == null || basePrice.signum() < 0) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precio base inválido");
    }

    existing.setCategory(cat);
    existing.setBrand(brand);
    existing.setName(name.trim());
    existing.setDescription(description);
    existing.setBasePrice(basePrice);

    return productRepo.save(existing);
}


}

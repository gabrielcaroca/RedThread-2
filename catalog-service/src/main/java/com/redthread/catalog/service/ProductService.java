package com.redthread.catalog.service;

import com.redthread.catalog.model.Brand;
import com.redthread.catalog.model.Category;
import com.redthread.catalog.model.Product;
import com.redthread.catalog.model.enums.ProductGender;
import com.redthread.catalog.repository.BrandRepository;
import com.redthread.catalog.repository.CategoryRepository;
import com.redthread.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final BrandRepository brandRepo;

    // CREATE
    public Product create(Long categoryId,
            Long brandId,
            String name,
            String description,
            BigDecimal basePrice,
            boolean featured,
            ProductGender gender) {

        if (basePrice == null || basePrice.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precio base inválido");
        }

        Category cat = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no existe"));

        Brand brand = null;
        if (brandId != null) {
            brand = brandRepo.findById(brandId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no existe"));
        }

        Product p = Product.builder()
                .category(cat)
                .brand(brand)
                .name(name)
                .description(description)
                .basePrice(basePrice)
                .active(true)
                .featured(featured)
                .gender(gender)
                .createdAt(Instant.now())
                .build();
        
        return productRepo.save(p);
    }

    public List<Product> getAll() {
        return productRepo.findAll();
    }

    // UPDATE
    public Product update(Long id,
            Long categoryId,
            Long brandId,
            String name,
            String description,
            BigDecimal basePrice,
            boolean featured,
            ProductGender gender) {

        Product existing = productRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no existe"));

        if (basePrice == null || basePrice.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precio base inválido");
        }

        Category cat = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no existe"));

        Brand brand = null;
        if (brandId != null) {
            brand = brandRepo.findById(brandId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no existe"));
        }

        existing.setCategory(cat);
        existing.setBrand(brand);
        existing.setName(name);
        existing.setDescription(description);
        existing.setBasePrice(basePrice);
        existing.setFeatured(featured);
        existing.setGender(gender);

        return productRepo.save(existing);
    }

    // LIST FILTERS
    public List<Product> list(Long categoryId, ProductGender gender, Boolean featured) {

        boolean featOnly = featured != null && featured;

        if (categoryId != null && gender != null && featOnly) {
            return productRepo.findByCategoryIdAndGenderAndFeaturedTrue(categoryId, gender);
        }
        if (categoryId != null && gender != null) {
            return productRepo.findByCategoryIdAndGender(categoryId, gender);
        }
        if (categoryId != null && featOnly) {
            return productRepo.findByCategoryIdAndFeaturedTrue(categoryId);
        }
        if (gender != null && featOnly) {
            return productRepo.findByGenderAndFeaturedTrue(gender);
        }
        if (categoryId != null) {
            return productRepo.findByCategoryId(categoryId);
        }
        if (gender != null) {
            return productRepo.findByGender(gender);
        }
        if (featOnly) {
            return productRepo.findByFeaturedTrue();
        }

        return productRepo.findAll();
    }
}

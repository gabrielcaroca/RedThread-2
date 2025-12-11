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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final BrandRepository brandRepo;

    // ============================================================
    // CREATE
    // ============================================================
    @Transactional
    public Product create(
            Long categoryId,
            Long brandId,
            String name,
            String description,
            BigDecimal basePrice,
            boolean featured,
            ProductGender gender
    ) {

        if (basePrice == null || basePrice.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precio base inválido");
        }

        Category cat = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoría no existe"
                ));

        Brand brand = null;
        if (brandId != null) {
            brand = brandRepo.findById(brandId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Marca no existe"
                    ));
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

        Product saved = productRepo.save(p);

        // Inicializar relaciones necesarias para evitar LazyInitializationException
        touchRelations(saved);
        return saved;
    }

    // ============================================================
    // GET BY ID (detalle de producto)
    // ============================================================
    public Product get(Long id) {
        // Usamos el método con EntityGraph que trae category y brand
        Product product = productRepo.findDetailById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no existe"
                ));

        // Inicializar relaciones antes de salir del método
        touchRelations(product);
        return product;
    }

    // ============================================================
    // GET ALL
    // ============================================================
    public List<Product> getAll() {
        List<Product> list = productRepo.findAll();
        list.forEach(this::touchRelations);
        return list;
    }

    // ============================================================
    // UPDATE
    // ============================================================
    @Transactional
    public Product update(
            Long id,
            Long categoryId,
            Long brandId,
            String name,
            String description,
            BigDecimal basePrice,
            boolean featured,
            ProductGender gender
    ) {

        Product existing = productRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no existe"
                ));

        if (basePrice == null || basePrice.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precio base inválido");
        }

        Category cat = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoría no existe"
                ));

        Brand brand = null;
        if (brandId != null) {
            brand = brandRepo.findById(brandId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Marca no existe"
                    ));
        }

        existing.setCategory(cat);
        existing.setBrand(brand);
        existing.setName(name);
        existing.setDescription(description);
        existing.setBasePrice(basePrice);
        existing.setFeatured(featured);
        existing.setGender(gender);

        Product saved = productRepo.save(existing);

        // Inicializar relaciones para JSON
        touchRelations(saved);
        return saved;
    }

    // ============================================================
    // LIST FILTERS (para home, tabs, etc.)
    // ============================================================
    public List<Product> list(Long categoryId, ProductGender gender, Boolean featured) {

        boolean featOnly = featured != null && featured;

        List<Product> result;

        if (categoryId != null && gender != null && featOnly) {
            result = productRepo.findByCategoryIdAndGenderAndFeaturedTrue(categoryId, gender);
        } else if (categoryId != null && gender != null) {
            result = productRepo.findByCategoryIdAndGender(categoryId, gender);
        } else if (categoryId != null && featOnly) {
            result = productRepo.findByCategoryIdAndFeaturedTrue(categoryId);
        } else if (gender != null && featOnly) {
            result = productRepo.findByGenderAndFeaturedTrue(gender);
        } else if (categoryId != null) {
            result = productRepo.findByCategoryId(categoryId);
        } else if (gender != null) {
            result = productRepo.findByGender(gender);
        } else if (featOnly) {
            result = productRepo.findByFeaturedTrue();
        } else {
            result = productRepo.findAll();
        }

        // Inicializar relaciones de todos los productos antes de devolver
        result.forEach(this::touchRelations);
        return result;
    }

    // ============================================================
    // Helper para inicializar relaciones LAZY
    // ============================================================
    private void touchRelations(Product p) {
        if (p.getCategory() != null) {
            p.getCategory().getId();
            p.getCategory().getName();
        }
        if (p.getBrand() != null) {
            p.getBrand().getId();
            p.getBrand().getName();
        }
    }
}

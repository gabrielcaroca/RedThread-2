package com.redthread.catalog.util;

import com.redthread.catalog.model.Brand;
import com.redthread.catalog.model.Category;
import com.redthread.catalog.repository.BrandRepository;
import com.redthread.catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final BrandRepository brandRepo;
    private final CategoryRepository categoryRepo;

    @Override
    public void run(String... args) {

        log.info("=== DataInitializer: precargando categorías y marcas ===");

        initCategories();
        initBrands();

        log.info("=== DataInitializer: completado ===");
    }

    private void initCategories() {
        createCategory("Zapatillas deportivas",
                "Calzado para correr, entrenar o uso diario.");

        createCategory("Poleras",
                "Ropa superior cómoda y urbana.");

        createCategory("Chaquetas",
                "Prendas exteriores para frío o estilo.");

        createCategory("Pantalones",
                "Jeans, joggers y ropa inferior casual.");
    }

    private void createCategory(String name, String description) {
        categoryRepo.findByNameIgnoreCase(name).ifPresentOrElse(
                c -> log.info("Categoría ya existe: {}", name),
                () -> {
                    Category newCat = Category.builder()
                            .name(name)
                            .description(description)
                            .active(true)
                            .build();
                    categoryRepo.save(newCat);
                    log.info("Categoría creada: {}", name);
                }
        );
    }

    private void initBrands() {
        createBrand("Nike");
        createBrand("Adidas");
        createBrand("Converse");
        createBrand("Puma");
    }

    private void createBrand(String name) {
        brandRepo.findByNameIgnoreCase(name).ifPresentOrElse(
                b -> log.info("Marca ya existe: {}", name),
                () -> {
                    Brand newBrand = Brand.builder()
                            .name(name)
                            .active(true)
                            .build();
                    brandRepo.save(newBrand);
                    log.info("Marca creada: {}", name);
                }
        );
    }
}

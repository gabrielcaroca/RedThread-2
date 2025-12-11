package com.redthread.catalog.repository;

import com.redthread.catalog.model.Product;
import com.redthread.catalog.model.enums.ProductGender;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Siempre queremos que al listar productos vengan ya con
     * category y brand cargados, porque spring.jpa.open-in-view=false
     * y luego Jackson no puede inicializar proxys en la respuesta.
     */

    @EntityGraph(attributePaths = {"category", "brand"})
    List<Product> findByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"category", "brand"})
    List<Product> findByGender(ProductGender gender);

    @EntityGraph(attributePaths = {"category", "brand"})
    List<Product> findByFeaturedTrue();

    @EntityGraph(attributePaths = {"category", "brand"})
    List<Product> findByGenderAndFeaturedTrue(ProductGender gender);

    @EntityGraph(attributePaths = {"category", "brand"})
    List<Product> findByCategoryIdAndGender(Long categoryId, ProductGender gender);

    @EntityGraph(attributePaths = {"category", "brand"})
    List<Product> findByCategoryIdAndFeaturedTrue(Long categoryId);

    @EntityGraph(attributePaths = {"category", "brand"})
    List<Product> findByCategoryIdAndGenderAndFeaturedTrue(
            Long categoryId,
            ProductGender gender
    );

    // También sobre-escribimos findAll con EntityGraph
    @Override
    @EntityGraph(attributePaths = {"category", "brand"})
    List<Product> findAll();

    // ========= NUEVO: detalle con relaciones =========
    /**
     * Usado para /products/{id} (detalle).
     * Carga category y brand para evitar proxies de Hibernate.
     */
    @EntityGraph(attributePaths = {"category", "brand"})
    Optional<Product> findDetailById(Long id);
}

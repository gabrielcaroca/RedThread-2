package com.redthread.catalog.repository;

import com.redthread.catalog.model.Product;
import com.redthread.catalog.model.enums.ProductGender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByGender(ProductGender gender);

    List<Product> findByFeaturedTrue();

    List<Product> findByGenderAndFeaturedTrue(ProductGender gender);

    List<Product> findByCategoryIdAndGender(Long categoryId, ProductGender gender);

    List<Product> findByCategoryIdAndFeaturedTrue(Long categoryId);

    List<Product> findByCategoryIdAndGenderAndFeaturedTrue(
            Long categoryId,
            ProductGender gender
    );
}

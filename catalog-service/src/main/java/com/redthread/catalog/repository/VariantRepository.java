package com.redthread.catalog.repository;

import com.redthread.catalog.model.Variant;
import com.redthread.catalog.model.enums.SizeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VariantRepository extends JpaRepository<Variant, Long> {
    Optional<Variant> findBySku(String sku);

    boolean existsByProductIdAndSizeTypeAndSizeValueAndColor(
            Long productId,
            SizeType sizeType,
            String sizeValue,
            String color
    );

    List<Variant> findByProductId(Long productId);
}

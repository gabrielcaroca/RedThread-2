package com.redthread.order.repository;

import com.redthread.order.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
  Optional<Cart> findByUserId(String userId);
}

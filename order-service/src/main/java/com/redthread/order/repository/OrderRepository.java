package com.redthread.order.repository;

import com.redthread.order.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
  List<Order> findByUserIdOrderByIdDesc(String userId);
  Optional<Order> findByIdAndUserId(Long id, String userId);
}
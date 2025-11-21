package com.redthread.order.repository;

import com.redthread.order.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {
  List<PaymentAttempt> findByOrderId(Long orderId);
}
package com.redthread.delivery.repository;

import com.redthread.delivery.domain.DeliveryRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRouteRepository extends JpaRepository<DeliveryRoute, Long> {
    List<DeliveryRoute> findByActivaTrueAndAssignedUserIdIsNull();
    Optional<DeliveryRoute> findByAssignedUserId(Long userId);
}

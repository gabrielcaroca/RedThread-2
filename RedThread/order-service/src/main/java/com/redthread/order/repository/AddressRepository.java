package com.redthread.order.repository;

import com.redthread.order.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
  List<Address> findByUserIdOrderByIdDesc(String userId);
  Optional<Address> findByIdAndUserId(Long id, String userId);
}










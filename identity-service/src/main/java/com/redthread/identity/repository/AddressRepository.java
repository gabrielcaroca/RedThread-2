package com.redthread.identity.repository;

import com.redthread.identity.model.Address;
import com.redthread.identity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    Optional<Address> findByIdAndUser(Long id, User user);
}

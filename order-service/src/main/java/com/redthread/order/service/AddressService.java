package com.redthread.order.service;

import com.redthread.order.dto.AddressReq;
import com.redthread.order.model.Address;
import com.redthread.order.repository.AddressRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {
  private final AddressRepository addressRepo;

  public List<Address> list(String userId) {
    return addressRepo.findByUserIdOrderByIdDesc(userId);
  }

  @Transactional
  public Address create(String userId, AddressReq req) {
    Address a = Address.builder()
        .userId(userId)
        .line1(req.line1())
        .line2(req.line2())
        .city(req.city())
        .state(req.state())
        .zip(req.zip())
        .country(req.country())
        .isDefault(Boolean.TRUE.equals(req.isDefault()))
        .build();
    return addressRepo.save(a);
  }

  @Transactional
  public Address patch(String userId, Long id, AddressReq req) {
    Address a = addressRepo.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new IllegalArgumentException("Address no encontrada"));
    if (req.line1()!=null) a.setLine1(req.line1());
    if (req.line2()!=null) a.setLine2(req.line2());
    if (req.city()!=null)  a.setCity(req.city());
    if (req.state()!=null) a.setState(req.state());
    if (req.zip()!=null)   a.setZip(req.zip());
    if (req.country()!=null) a.setCountry(req.country());
    if (req.isDefault()!=null) a.setDefault(req.isDefault());
    return addressRepo.save(a);
  }

  @Transactional
  public void delete(String userId, Long id) {
    Address a = addressRepo.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new IllegalArgumentException("Address no encontrada"));
    addressRepo.delete(a);
  }
}

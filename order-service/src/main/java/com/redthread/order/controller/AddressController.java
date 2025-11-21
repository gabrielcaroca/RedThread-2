package com.redthread.order.controller;

import com.redthread.order.dto.AddressReq;
import com.redthread.order.dto.AddressRes;
import com.redthread.order.model.Address;
import com.redthread.order.security.JwtUserResolver;
import com.redthread.order.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {

  private final AddressService svc;
  private final JwtUserResolver auth;

  @GetMapping
  public List<AddressRes> list() {
    return svc.list(auth.currentUserId()).stream()
        .map(a -> new AddressRes(a.getId(), a.getLine1(), a.getLine2(), a.getCity(), a.getState(), a.getZip(), a.getCountry(), a.isDefault()))
        .toList();
  }

  @PostMapping
  public AddressRes create(@Valid @RequestBody AddressReq req) {
    Address a = svc.create(auth.currentUserId(), req);
    return new AddressRes(a.getId(), a.getLine1(), a.getLine2(), a.getCity(), a.getState(), a.getZip(), a.getCountry(), a.isDefault());
  }

  @PatchMapping("/{id}")
  public AddressRes patch(@PathVariable Long id, @Valid @RequestBody AddressReq req) {
    Address a = svc.patch(auth.currentUserId(), id, req);
    return new AddressRes(a.getId(), a.getLine1(), a.getLine2(), a.getCity(), a.getState(), a.getZip(), a.getCountry(), a.isDefault());
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    svc.delete(auth.currentUserId(), id);
  }
}

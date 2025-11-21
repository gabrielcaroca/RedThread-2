package com.redthread.identity.controller;

import com.redthread.identity.dto.AddressDto;
import com.redthread.identity.model.Address;
import com.redthread.identity.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
public class AddressController {
    private final AddressService service;
    public AddressController(AddressService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<List<Address>> list() { return ResponseEntity.ok(service.listMine()); }

    @PostMapping
    public ResponseEntity<Address> create(@Valid @RequestBody AddressDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Address> update(@PathVariable Long id, @Valid @RequestBody AddressDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

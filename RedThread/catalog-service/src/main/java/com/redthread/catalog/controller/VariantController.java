package com.redthread.catalog.controller;

import com.redthread.catalog.controller.dto.CreateVariantReq;
import com.redthread.catalog.model.Variant;
import com.redthread.catalog.repository.VariantRepository;
import com.redthread.catalog.service.VariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/variants")
@RequiredArgsConstructor
public class VariantController {
    private final VariantService service;
    private final VariantRepository repo;

    @PostMapping
    public ResponseEntity<Variant> create(@RequestBody @Valid CreateVariantReq req) {
        Variant created = service.create(
                req.productId(),
                req.sizeType(),
                req.sizeValue(),
                req.color(),
                req.sku(),
                req.priceOverride());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public Variant get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public List<Variant> list(@RequestParam Long productId) {
        return service.byProduct(productId);
    }

    @GetMapping("/todos")
    public List<Variant> all() {
        return repo.findAll();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ResponseStatusException ex) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(Map.of("message", ex.getReason()));
    }

}

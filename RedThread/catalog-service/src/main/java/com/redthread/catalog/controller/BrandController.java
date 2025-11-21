package com.redthread.catalog.controller;

import com.redthread.catalog.controller.dto.CreateBrandReq;
import com.redthread.catalog.model.Brand;
import com.redthread.catalog.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService service;

    @PostMapping
    public Brand create(@RequestBody @Valid CreateBrandReq req) {
        return service.create(req.name());
    }

    @GetMapping("/{id}")
    public Brand get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public List<Brand> getAll() {
        return service.getAll();
    }

    /**
     * Este es el manejador de errores.
     * Captura la 'IllegalArgumentException' que lanza tu servicio
     * y la convierte en un error 400 (Bad Request) con un JSON.
     */
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        // Devuelve un error 400 (BAD_REQUEST)
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
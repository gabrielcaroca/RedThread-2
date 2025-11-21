package com.redthread.catalog.controller;

import com.redthread.catalog.controller.dto.CreateProductReq;
import com.redthread.catalog.model.Product;
import com.redthread.catalog.repository.ProductRepository;
import com.redthread.catalog.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService service;
    private final ProductRepository repo;

    @PostMapping
    public Product create(@RequestBody @Valid CreateProductReq req) {
        return service.create(req.categoryId(), req.brandId(), req.name(), req.description(), req.basePrice());
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody @Valid CreateProductReq req) {
        return service.update(id, req.categoryId(), req.brandId(), req.name(), req.description(), req.basePrice());
    }

    @GetMapping
    public List<Product> list(@RequestParam(required = false) Long categoryId) {
        return (categoryId == null) ? repo.findAll() : service.byCategory(categoryId);
    }

    

}

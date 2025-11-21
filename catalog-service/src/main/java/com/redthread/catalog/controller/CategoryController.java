package com.redthread.catalog.controller;

import com.redthread.catalog.controller.dto.CreateCategoryReq;
import com.redthread.catalog.model.Category;
import com.redthread.catalog.repository.CategoryRepository;
import com.redthread.catalog.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService service;

    @PostMapping
    public Category create(@RequestBody @Valid CreateCategoryReq req) {
        return service.create(req.name(), req.description());
    }

    @GetMapping
    public List<Category> list() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Category get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @RequestBody @Valid CreateCategoryReq req) {
        return service.update(id, req.name(), req.description());
    }

}

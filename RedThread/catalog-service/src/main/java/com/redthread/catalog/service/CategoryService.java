package com.redthread.catalog.service;

import com.redthread.catalog.model.Category;
import com.redthread.catalog.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository repo;

    public Category create(String name, String description) {
        repo.findByNameIgnoreCase(name).ifPresent(c -> {
            throw new IllegalArgumentException("Categoria ya existe");
        });
        Category c = Category.builder().name(name.trim()).description(description).active(true).build();
        return repo.save(c);
    }

    public Category get(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }

    public List<Category> getAll() {
        return repo.findAll();
    }

    public Category update(Long id, String name, String description) {
        Category existing = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));

        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre inválido");
        }

        // Verifica duplicado (otro registro con el mismo nombre)
        repo.findByNameIgnoreCase(name)
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre");
                });

        existing.setName(name.trim());
        existing.setDescription(description != null ? description.trim() : null);

        return repo.save(existing);
    }

}

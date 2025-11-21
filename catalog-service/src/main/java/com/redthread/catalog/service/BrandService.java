package com.redthread.catalog.service;

import com.redthread.catalog.model.Brand;
import com.redthread.catalog.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository repo;

    public Brand create(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre inválido");
        }

        repo.findByNameIgnoreCase(name)
                .ifPresent(b -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "Marca ya existe"); });

        Brand b = Brand.builder()
                .name(name.trim())
                .active(true)
                .createdAt(Instant.now())
                .build();

        return repo.save(b);
    }

    public Brand get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Marca no encontrada"
                ));
    }

    public List<Brand> getAll() {
    return repo.findAll();
}
}

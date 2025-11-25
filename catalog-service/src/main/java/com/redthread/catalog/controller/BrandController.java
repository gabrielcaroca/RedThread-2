package com.redthread.catalog.controller;

import com.redthread.catalog.controller.dto.CreateBrandReq;
import com.redthread.catalog.model.Brand;
import com.redthread.catalog.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
@Tag(name = "Brands", description = "Marcas de productos")
public class BrandController {

    private final BrandService service;

    @PostMapping
    @Operation(summary = "Crear marca")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Marca creada",
                    content = @Content(schema = @Schema(implementation = Brand.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Marca duplicada")
    })
    public ResponseEntity<Brand> create(@RequestBody @Valid CreateBrandReq req) {
        Brand created = service.create(req.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener marca por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Marca encontrada",
                    content = @Content(schema = @Schema(implementation = Brand.class))),
            @ApiResponse(responseCode = "404", description = "Marca no existe")
    })
    public Brand get(@Parameter(description = "ID de marca") @PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    @Operation(summary = "Listar marcas")
    public List<Brand> list() {
        return service.getAll();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}

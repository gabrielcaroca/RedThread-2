package com.redthread.catalog.controller;

import com.redthread.catalog.controller.dto.CreateCategoryReq;
import com.redthread.catalog.model.Category;
import com.redthread.catalog.service.CategoryService;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
@Tag(name = "Categories", description = "Gestión de categorías del catálogo")
public class CategoryController {

        private final CategoryService service;

        @PostMapping
        @Operation(summary = "Crear categoría", description = "Crea una nueva categoría. El nombre es único.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Categoría creada", content = @Content(schema = @Schema(implementation = Category.class))),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                        @ApiResponse(responseCode = "409", description = "Categoría duplicada"),
                        @ApiResponse(responseCode = "500", description = "Error interno")
        })
        public ResponseEntity<Category> create(@RequestBody @Valid CreateCategoryReq req) {
                Category created = service.create(req.name(), req.description());
                return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }

        @GetMapping
        @Operation(summary = "Listar categorías", description = "Obtiene todas las categorías.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Listado de categorías"),
                        @ApiResponse(responseCode = "500", description = "Error interno")
        })
        public List<Category> list() {
                return service.getAll();
        }

        @GetMapping("/{id}")
        @Operation(summary = "Obtener categoría por ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Categoría encontrada", content = @Content(schema = @Schema(implementation = Category.class))),
                        @ApiResponse(responseCode = "404", description = "Categoría no existe"),
                        @ApiResponse(responseCode = "500", description = "Error interno")
        })
        public Category get(
                        @Parameter(description = "ID de la categoría", example = "1") @PathVariable Long id) {
                return service.get(id);
        }

        @PutMapping("/{id}")
        @Operation(summary = "Actualizar categoría")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Categoría actualizada", content = @Content(schema = @Schema(implementation = Category.class))),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                        @ApiResponse(responseCode = "404", description = "Categoría no existe"),
                        @ApiResponse(responseCode = "409", description = "Nombre duplicado"),
                        @ApiResponse(responseCode = "500", description = "Error interno")
        })
        public Category update(
                        @Parameter(description = "ID de la categoría", example = "1") @PathVariable Long id,
                        @RequestBody @Valid CreateCategoryReq req) {
                return service.update(id, req.name(), req.description());
        }

}

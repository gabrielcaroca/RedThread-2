package com.redthread.catalog.controller;

import com.redthread.catalog.controller.dto.CreateProductReq;
import com.redthread.catalog.model.Product;
import com.redthread.catalog.model.enums.ProductGender;
import com.redthread.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Gestión del catálogo de productos")
public class ProductController {

        private final ProductService service;

        // ============================================================
        // CREAR
        // ============================================================
        @PostMapping
        @Operation(summary = "Crear producto")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Producto creado correctamente", content = @Content(schema = @Schema(implementation = Product.class))),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                        @ApiResponse(responseCode = "404", description = "Categoría o marca no existe"),
                        @ApiResponse(responseCode = "500", description = "Error interno al crear producto")
        })
        public org.springframework.http.ResponseEntity<Product> create(@RequestBody @Valid CreateProductReq req) {
                Product created = service.create(
                                req.categoryId(),
                                req.brandId(),
                                req.name(),
                                req.description(),
                                req.basePrice(),
                                req.featured(),
                                req.gender());
                return org.springframework.http.ResponseEntity
                                .status(org.springframework.http.HttpStatus.CREATED)
                                .body(created);
        }

        // ============================================================
        // LISTAR TODOS SIN FILTROS
        // ============================================================
        @GetMapping("/all")
        @Operation(summary = "Listar todos los productos sin filtros")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Listado de productos"),
                        @ApiResponse(responseCode = "500", description = "Error interno")
        })
        public List<Product> getAll() {
                return service.getAll();
        }

        // ============================================================
        // ACTUALIZAR
        // ============================================================
        @PutMapping("/{id}")
        @Operation(summary = "Actualizar producto")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente", content = @Content(schema = @Schema(implementation = Product.class))),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                        @ApiResponse(responseCode = "404", description = "Producto, categoría o marca no existe"),
                        @ApiResponse(responseCode = "500", description = "Error interno al actualizar producto")
        })
        public Product update(
                        @Parameter(description = "ID del producto") @PathVariable Long id,
                        @RequestBody @Valid CreateProductReq req) {
                return service.update(
                                id,
                                req.categoryId(),
                                req.brandId(),
                                req.name(),
                                req.description(),
                                req.basePrice(),
                                req.featured(),
                                req.gender());
        }

        // ============================================================
        // LISTAR CON FILTROS (home, tabs, etc.)
        // ============================================================
        @GetMapping
        @Operation(summary = "Listar productos", description = "Filtros opcionales:\n" +
                        "- featured=true → solo destacados\n" +
                        "- gender=HOMBRE|MUJER\n" +
                        "- categoryId → combina con los anteriores")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Listado de productos"),
                        @ApiResponse(responseCode = "400", description = "Parámetros de filtro inválidos"),
                        @ApiResponse(responseCode = "500", description = "Error interno")
        })
        public List<Product> list(
                        @Parameter(description = "Filtra por categoría (opcional)") @RequestParam(required = false) Long categoryId,

                        @Parameter(description = "Filtra por género (opcional)") @RequestParam(required = false) ProductGender gender,

                        @Parameter(description = "Solo destacados (opcional)") @RequestParam(required = false) Boolean featured) {
                return service.list(categoryId, gender, featured);
        }

        // ============================================================
        // GET BY ID (detalle para la app)
        // ============================================================
        @GetMapping("/{id}")
        @Operation(summary = "Obtener producto por ID (detalle)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Producto encontrado", content = @Content(schema = @Schema(implementation = Product.class))),
                        @ApiResponse(responseCode = "404", description = "Producto no existe"),
                        @ApiResponse(responseCode = "500", description = "Error interno")
        })
        public Product getById(
                        @Parameter(description = "ID del producto") @PathVariable Long id) {
                return service.get(id);
        }
}

package com.redthread.catalog.controller;

import com.redthread.catalog.controller.dto.CreateVariantReq;
import com.redthread.catalog.controller.dto.VariantDto;
import com.redthread.catalog.controller.dto.VariantMapper;
import com.redthread.catalog.model.Variant;
import com.redthread.catalog.repository.VariantRepository;
import com.redthread.catalog.service.VariantService;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/variants")
@RequiredArgsConstructor
@Tag(name = "Variants", description = "Variantes por producto (talla/color/SKU/precio override) + stock inicial")
public class VariantController {

        private final VariantService service;
        private final VariantRepository repo;

        @PostMapping
        @Operation(summary = "Crear variante", description = "Crea una variante asociada a un producto. "
                        + "Si no se envía SKU, se genera automáticamente. "
                        + "Crea inventario con stock inicial.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Variante creada correctamente", content = @Content(schema = @Schema(implementation = VariantDto.class))),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos o talla inválida"),
                        @ApiResponse(responseCode = "404", description = "Producto no existe"),
                        @ApiResponse(responseCode = "409", description = "Variante duplicada o SKU en uso"),
                        @ApiResponse(responseCode = "500", description = "Error interno al crear variante")
        })
        public ResponseEntity<VariantDto> create(@RequestBody @Valid CreateVariantReq req) {

                Variant saved = service.create(req);
                VariantDto dto = VariantMapper.toDto(saved);

                return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Obtener variante por ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Variante encontrada", content = @Content(schema = @Schema(implementation = VariantDto.class))),
                        @ApiResponse(responseCode = "404", description = "Variante no existe"),
                        @ApiResponse(responseCode = "500", description = "Error interno")
        })
        public Variant get(@Parameter(description = "ID de la variante") @PathVariable Long id) {
                return service.get(id);
        }

        @GetMapping
        @Operation(summary = "Listar variantes por producto")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Listado de variantes"),
                        @ApiResponse(responseCode = "404", description = "Producto no existe"),
                        @ApiResponse(responseCode = "500", description = "Error interno")
        })
        public List<Variant> list(
                        @Parameter(description = "ID del producto padre") @RequestParam Long productId) {
                return service.byProduct(productId);
        }

        @GetMapping("/todos")
        @Operation(summary = "Listar todas las variantes (debug)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Listado completo de variantes"),
                        @ApiResponse(responseCode = "500", description = "Error interno")
        })
        public List<Variant> all() {
                return repo.findAll();
        }

        @PutMapping("/{id}")
        @Operation(summary = "Actualizar variante")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Variante actualizada correctamente", content = @Content(schema = @Schema(implementation = VariantDto.class))),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                        @ApiResponse(responseCode = "404", description = "Variante o producto no existe"),
                        @ApiResponse(responseCode = "409", description = "Conflicto de combinación o SKU"),
                        @ApiResponse(responseCode = "500", description = "Error interno al actualizar variante")
        })
        public VariantDto update(
                        @Parameter(description = "ID de la variante") @PathVariable Long id,
                        @RequestBody @Valid CreateVariantReq req) {
                Variant updated = service.update(id, req);
                return VariantMapper.toDto(updated);
        }

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<Map<String, String>> handleConflict(ResponseStatusException ex) {
                return ResponseEntity
                                .status(ex.getStatusCode())
                                .body(Map.of("message", ex.getReason()));
        }
}

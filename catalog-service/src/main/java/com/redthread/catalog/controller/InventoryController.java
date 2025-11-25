package com.redthread.catalog.controller;

import com.redthread.catalog.controller.dto.AdjustStockReq;
import com.redthread.catalog.model.Inventory;
import com.redthread.catalog.service.InventoryService;
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

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Stock disponible/reservado por variante")
public class InventoryController {

    private final InventoryService service;

    @GetMapping("/by-variant/{variantId}")
    @Operation(
            summary = "Obtener inventario por variante",
            description = "Retorna el inventario asociado a una variante."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventario encontrado",
                    content = @Content(schema = @Schema(implementation = Inventory.class))),
            @ApiResponse(responseCode = "404", description = "Variante no existe / sin inventario")
    })
    public Inventory byVariant(
            @Parameter(description = "ID de la variante", example = "5")
            @PathVariable Long variantId
    ) {
        return service.getByVariant(variantId);
    }

    @PostMapping("/adjust")
    @Operation(
            summary = "Ajustar stock disponible",
            description = "Aplica un delta (positivo o negativo) al stockAvailable de la variante."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock ajustado",
                    content = @Content(schema = @Schema(implementation = Inventory.class))),
            @ApiResponse(responseCode = "400", description = "Delta inválido"),
            @ApiResponse(responseCode = "404", description = "Variante no existe")
    })
    public Inventory adjust(@RequestBody @Valid AdjustStockReq req) {
        return service.adjustStock(req.variantId(), req.delta());
    }
}

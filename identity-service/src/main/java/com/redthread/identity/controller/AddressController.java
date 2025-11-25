package com.redthread.identity.controller;

import com.redthread.identity.dto.AddressDto;
import com.redthread.identity.model.Address;
import com.redthread.identity.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Address", description = "Direcciones del usuario autenticado")
@RestController
@RequestMapping("/addresses")
public class AddressController {

    private final AddressService addresses;

    public AddressController(AddressService addresses) {
        this.addresses = addresses;
    }

    @Operation(summary = "Listar mis direcciones")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista OK"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping
    public ResponseEntity<List<Address>> listMine() {
        return ResponseEntity.ok(addresses.listMine());
    }

    @Operation(summary = "Crear dirección")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Creada OK"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<Address> create(@RequestBody AddressDto dto) {
        return ResponseEntity.ok(addresses.create(dto));
    }

    @Operation(summary = "Actualizar dirección")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizada OK"),
            @ApiResponse(responseCode = "404", description = "No existe"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Address> update(@PathVariable Long id, @RequestBody AddressDto dto) {
        return ResponseEntity.ok(addresses.update(id, dto));
    }

    @Operation(summary = "Eliminar dirección")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminada"),
            @ApiResponse(responseCode = "404", description = "No existe"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addresses.delete(id);
        return ResponseEntity.noContent().build();
    }
}

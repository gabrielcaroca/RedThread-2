package com.redthread.order.controller;

import com.redthread.order.dto.AddressReq;
import com.redthread.order.dto.AddressRes;
import com.redthread.order.model.Address;
import com.redthread.order.security.JwtUserResolver;
import com.redthread.order.service.AddressService;
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
@RequestMapping("/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "Direcciones del usuario autenticado")
public class AddressController {

  private final AddressService svc;
  private final JwtUserResolver auth;

  @Operation(summary = "Listar direcciones")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Lista de direcciones",
          content = @Content(schema = @Schema(implementation = AddressRes.class))),
      @ApiResponse(responseCode = "401", description = "No autenticado")
  })
  @GetMapping
  public List<AddressRes> list() {
    return svc.list(auth.currentUserId()).stream()
        .map(a -> new AddressRes(
            a.getId(), a.getLine1(), a.getLine2(),
            a.getCity(), a.getState(), a.getZip(),
            a.getCountry(), a.isDefault()
        )).toList();
  }

  @Operation(summary = "Crear dirección")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Dirección creada",
          content = @Content(schema = @Schema(implementation = AddressRes.class))),
      @ApiResponse(responseCode = "400", description = "Datos inválidos"),
      @ApiResponse(responseCode = "401", description = "No autenticado")
  })
  @PostMapping
  public AddressRes create(@Valid @RequestBody AddressReq req) {
    Address a = svc.create(auth.currentUserId(), req);
    return new AddressRes(
        a.getId(), a.getLine1(), a.getLine2(),
        a.getCity(), a.getState(), a.getZip(),
        a.getCountry(), a.isDefault()
    );
  }

  @Operation(summary = "Actualizar parcialmente dirección")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Dirección actualizada",
          content = @Content(schema = @Schema(implementation = AddressRes.class))),
      @ApiResponse(responseCode = "400", description = "Dirección no encontrada o datos inválidos"),
      @ApiResponse(responseCode = "401", description = "No autenticado")
  })
  @PatchMapping("/{id}")
  public AddressRes patch(
      @Parameter(description = "ID de la dirección", example = "3")
      @PathVariable Long id,
      @Valid @RequestBody AddressReq req
  ) {
    Address a = svc.patch(auth.currentUserId(), id, req);
    return new AddressRes(
        a.getId(), a.getLine1(), a.getLine2(),
        a.getCity(), a.getState(), a.getZip(),
        a.getCountry(), a.isDefault()
    );
  }

  @Operation(summary = "Eliminar dirección")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Dirección eliminada"),
      @ApiResponse(responseCode = "400", description = "Dirección no encontrada"),
      @ApiResponse(responseCode = "401", description = "No autenticado")
  })
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(description = "ID de la dirección", example = "3")
      @PathVariable Long id
  ) {
    svc.delete(auth.currentUserId(), id);
  }
}
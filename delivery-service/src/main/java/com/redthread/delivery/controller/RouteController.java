package com.redthread.delivery.controller;

import com.redthread.delivery.domain.DeliveryRoute;
import com.redthread.delivery.domain.Shipment;
import com.redthread.delivery.dto.route.CreateRouteRequest;
import com.redthread.delivery.dto.route.RouteResponse;
import com.redthread.delivery.dto.shipment.ShipmentResponse;
import com.redthread.delivery.security.AuthUtils;
import com.redthread.delivery.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
@Tag(name="Routes", description="Rutas de despacho (creación, toma y consulta)")
public class RouteController {

    private final RouteService routeService;
    private final AuthUtils auth;

    @PostMapping
    @Operation(summary="Crear ruta", description="Solo ADMIN puede crear rutas.")
    @ApiResponses({
            @ApiResponse(responseCode="201", description="Ruta creada"),
            @ApiResponse(responseCode="400", description="Datos inválidos"),
            @ApiResponse(responseCode="401", description="No autenticado"),
            @ApiResponse(responseCode="403", description="No es admin")
    })
    public RouteResponse create(@Valid @RequestBody CreateRouteRequest req,
                                @AuthenticationPrincipal Jwt jwt) {

        if (!auth.hasAdmin(jwt)) throw new SecurityException("Only admin can create routes");

        Long adminId = auth.getCurrentUserId(jwt);
        DeliveryRoute r = routeService.create(req, adminId, jwt);
        return map(r);
    }

    @GetMapping("/active")
    @Operation(summary="Rutas activas", description="Lista todas las rutas activas.")
    public List<RouteResponse> active(@AuthenticationPrincipal Jwt jwt) {
        return routeService.activeRoutes().stream().map(this::map).toList();
    }

    @PostMapping("/{id}/take")
    @Operation(summary="Tomar ruta", description="Driver toma una ruta activa sin asignación previa.")
    @ApiResponses({
            @ApiResponse(responseCode="200", description="Ruta tomada"),
            @ApiResponse(responseCode="400", description="Ruta no disponible / ya tienes una ruta"),
            @ApiResponse(responseCode="401", description="No autenticado")
    })
    public RouteResponse take(
            @Parameter(description="ID de la ruta") @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long driverId = auth.getCurrentUserId(jwt);
        DeliveryRoute r = routeService.takeRoute(id, driverId);
        return map(r);
    }

    @GetMapping("/{id}/shipments")
    @Operation(summary="Envios de una ruta", description="Admin o driver asignado puede ver los envíos.")
    @ApiResponses({
            @ApiResponse(responseCode="200", description="Listado de envíos"),
            @ApiResponse(responseCode="401", description="No autenticado"),
            @ApiResponse(responseCode="403", description="No autorizado")
    })
    public List<ShipmentResponse> shipments(
            @Parameter(description="ID de la ruta") @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);

        List<Shipment> list = routeService.shipmentsByRoute(id, userId, isAdmin);
        return list.stream().map(ShipmentResponse::from).toList();
    }

    private RouteResponse map(DeliveryRoute r) {
        return new RouteResponse(
                r.getId(),
                r.getNombre(),
                r.getDescripcion(),
                r.getTotalPedidos(),
                r.getTotalPrice(),
                r.getActiva(),
                r.getAssignedUserId()
        );
    }
}

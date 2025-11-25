package com.redthread.delivery.controller;

import com.redthread.delivery.domain.Shipment;
import com.redthread.delivery.domain.TrackingEvent;
import com.redthread.delivery.dto.shipment.*;
import com.redthread.delivery.security.AuthUtils;
import com.redthread.delivery.service.AssignmentService;
import com.redthread.delivery.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shipments")
@RequiredArgsConstructor
@Tag(name="Shipments", description="Envíos de órdenes + tracking + evidencia")
public class ShipmentController {

    private final ShipmentService service;
    private final AssignmentService assignmentService;
    private final AuthUtils auth;

    @PostMapping
    @Operation(summary="Crear shipment", description="Crea un envío desde un orderId.")
    @ApiResponses({
            @ApiResponse(responseCode="201", description="Shipment creado"),
            @ApiResponse(responseCode="400", description="Datos inválidos / orden inválida"),
            @ApiResponse(responseCode="401", description="No autenticado")
    })
    public ShipmentResponse create(@Valid @RequestBody CreateShipmentRequest req,
                                   @AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        Shipment s = service.createFromOrder(req, userId, jwt);
        return ShipmentResponse.from(s);
    }

    @GetMapping
    @Operation(summary="Listar mis shipments", description="Admin ve todos, usuario ve los suyos.")
    public List<ShipmentResponse> listMine(@AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        return service.listMine(userId, isAdmin).stream().map(ShipmentResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary="Detalle shipment")
    @ApiResponses({
            @ApiResponse(responseCode="200", description="Shipment encontrado"),
            @ApiResponse(responseCode="404", description="No existe"),
            @ApiResponse(responseCode="403", description="No autorizado")
    })
    public ShipmentResponse get(
            @Parameter(description="ID del shipment") @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        return ShipmentResponse.from(service.getFor(id, userId, isAdmin));
    }

    @PostMapping("/{id}/assign")
    @Operation(summary="Asignar shipment a driver", description="Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode="200", description="Asignado"),
            @ApiResponse(responseCode="403", description="No es admin")
    })
    public Map<String, Object> assign(
            @Parameter(description="ID del shipment") @PathVariable Long id,
            @Valid @RequestBody AssignRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (!auth.hasAdmin(jwt)) throw new SecurityException("Only admin");
        assignmentService.assign(id, req.assignedUserId());
        return Map.of("ok", true);
    }

    @PostMapping("/{id}/start")
    @Operation(summary="Iniciar envío", description="Pasa a IN_TRANSIT.")
    public ShipmentResponse start(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        return ShipmentResponse.from(service.start(id, userId, isAdmin));
    }

    @PostMapping(value="/{id}/delivered", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary="Marcar entregado (con evidencia)", description="Multipart con foto obligatoria.")
    @ApiResponses({
            @ApiResponse(responseCode="200", description="Marcado entregado"),
            @ApiResponse(responseCode="400", description="Datos inválidos"),
            @ApiResponse(responseCode="403", description="No autorizado"),
            @ApiResponse(responseCode="404", description="Shipment no existe")
    })
    public ShipmentResponse delivered(
            @PathVariable Long id,
            @RequestPart("receiverName") String receiverName,
            @RequestPart(value="note", required=false) String note,
            @RequestPart("photo") MultipartFile photo,
            @RequestPart(value="latitude", required=false) BigDecimal latitude,
            @RequestPart(value="longitude", required=false) BigDecimal longitude,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);

        Shipment s = service.delivered(
                id, receiverName, note, photo,
                latitude, longitude,
                userId, isAdmin, jwt
        );
        return ShipmentResponse.from(s);
    }

    @PostMapping(value="/{id}/fail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary="Marcar fallo (con evidencia)", description="Multipart con foto obligatoria.")
    @ApiResponses({
            @ApiResponse(responseCode="200", description="Marcado fallido"),
            @ApiResponse(responseCode="400", description="Datos inválidos"),
            @ApiResponse(responseCode="403", description="No autorizado"),
            @ApiResponse(responseCode="404", description="Shipment no existe")
    })
    public ShipmentResponse fail(
            @PathVariable Long id,
            @RequestPart(value="note", required=false) String note,
            @RequestPart("photo") MultipartFile photo,
            @RequestPart(value="latitude", required=false) BigDecimal latitude,
            @RequestPart(value="longitude", required=false) BigDecimal longitude,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);

        Shipment s = service.fail(
                id, note, photo,
                latitude, longitude,
                userId, isAdmin, jwt
        );
        return ShipmentResponse.from(s);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary="Cancelar shipment")
    public ShipmentResponse cancel(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        return ShipmentResponse.from(service.cancel(id, userId, isAdmin));
    }

    @PostMapping("/{id}/track")
    @Operation(summary="Registrar tracking manual")
    @ApiResponses({
            @ApiResponse(responseCode="200", description="Tracking registrado"),
            @ApiResponse(responseCode="400", description="Datos inválidos"),
            @ApiResponse(responseCode="403", description="No autorizado"),
            @ApiResponse(responseCode="404", description="Shipment no existe")
    })
    public Map<String,Object> track(
            @PathVariable Long id,
            @Valid @RequestBody TrackRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        TrackingEvent ev = service.track(id, req, userId, isAdmin);

        return Map.of(
                "id", ev.getId(),
                "status", ev.getStatus(),
                "latitude", ev.getLatitude(),
                "longitude", ev.getLongitude(),
                "note", ev.getNote(),
                "createdAt", ev.getCreatedAt()
        );
    }

    @GetMapping("/{id}/tracking")
    @Operation(summary="Listar tracking")
    @ApiResponses({
            @ApiResponse(responseCode="200", description="Lista de tracking"),
            @ApiResponse(responseCode="403", description="No autorizado"),
            @ApiResponse(responseCode="404", description="Shipment no existe")
    })
    public List<Map<String,Object>> tracking(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);

        List<TrackingEvent> list = service.listTracking(id, userId, isAdmin);

        return list.stream()
                .map(ev -> Map.<String, Object>of(
                        "id", ev.getId(),
                        "status", ev.getStatus(),
                        "latitude", ev.getLatitude(),
                        "longitude", ev.getLongitude(),
                        "note", ev.getNote(),
                        "createdAt", ev.getCreatedAt()
                ))
                .toList();
    }

    @GetMapping("/assigned-to-me")
    @Operation(summary="Shipments asignados a mi")
    public List<ShipmentResponse> assignedToMe(@AuthenticationPrincipal Jwt jwt) {
        Long userId = auth.getCurrentUserId(jwt);
        boolean isAdmin = auth.hasAdmin(jwt);
        return service.listAssignedToMe(userId, isAdmin).stream().map(ShipmentResponse::from).toList();
    }
}

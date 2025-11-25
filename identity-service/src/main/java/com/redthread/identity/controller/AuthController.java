package com.redthread.identity.controller;

import com.redthread.identity.dto.JwtResponse;
import com.redthread.identity.dto.LoginRequest;
import com.redthread.identity.dto.RegisterRequest;
import com.redthread.identity.dto.ResetPasswordRequest;
import com.redthread.identity.dto.ResetPasswordConfirmRequest;
import com.redthread.identity.dto.VerifyTokenRequest;
import com.redthread.identity.service.AuthService;
import com.redthread.identity.service.AuthResetPasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Registro, login y manejo de JWT")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService auth;
    private final AuthResetPasswordService resetService;

    public AuthController(AuthService auth, AuthResetPasswordService resetService) {
        this.auth = auth;
        this.resetService = resetService;
    }

    @Operation(summary = "Registrar usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registrado OK",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email ya registrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(auth.register(req));
    }

    @Operation(summary = "Login")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login OK",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(auth.login(req));
    }

    @Operation(summary = "Verificar token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token válido"),
            @ApiResponse(responseCode = "401", description = "Token inválido o expirado")
    })
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyTokenRequest req) {
        auth.verify(req.getToken());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refresh OK",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token inválido o expirado")
    })
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@Valid @RequestBody VerifyTokenRequest req) {
        return ResponseEntity.ok(auth.refresh(req.getToken()));
    }

    @Operation(summary = "Solicitar reset de contraseña (simulado)")
    @PostMapping("/reset-password")
    public ResponseEntity<?> requestReset(@RequestBody ResetPasswordRequest req) {
        boolean exists = resetService.checkUserExists(req.identifier());
        if (!exists) return ResponseEntity.badRequest().body("Usuario no encontrado");
        return ResponseEntity.ok("Código enviado (simulado)");
    }

    @Operation(summary = "Confirmar reset de contraseña (simulado)")
    @PostMapping("/reset-password/confirm")
    public ResponseEntity<?> confirmReset(@RequestBody ResetPasswordConfirmRequest req) {
        boolean ok = resetService.resetPassword(req.identifier(), req.newPassword());
        if (!ok) return ResponseEntity.badRequest().body("No se pudo cambiar la contraseña");
        return ResponseEntity.ok("Contraseña actualizada exitosamente");
    }
}

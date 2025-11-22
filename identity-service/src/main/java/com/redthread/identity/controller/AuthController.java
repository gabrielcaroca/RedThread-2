package com.redthread.identity.controller;

import com.redthread.identity.dto.JwtResponse;
import com.redthread.identity.dto.LoginRequest;
import com.redthread.identity.dto.RegisterRequest;
import com.redthread.identity.dto.ResetPasswordRequest;
import com.redthread.identity.dto.ResetPasswordConfirmRequest;
import com.redthread.identity.service.AuthService;
import com.redthread.identity.service.AuthResetPasswordService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService auth;
    private final AuthResetPasswordService resetService;

    public AuthController(AuthService auth, AuthResetPasswordService resetService) {
        this.auth = auth;
        this.resetService = resetService;
    }

   
    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(auth.register(req));
    }

    
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(auth.login(req));
    }

   
    @PostMapping("/reset-password")
    public ResponseEntity<?> requestReset(@RequestBody ResetPasswordRequest req) {
        boolean exists = resetService.checkUserExists(req.identifier());
        if (!exists) {
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }
        return ResponseEntity.ok("Código enviado (simulado)");
    }

   
    @PostMapping("/reset-password/confirm")
    public ResponseEntity<?> confirmReset(@RequestBody ResetPasswordConfirmRequest req) {
        boolean ok = resetService.resetPassword(req.identifier(), req.newPassword());
        if (!ok) {
            return ResponseEntity.badRequest().body("No se pudo cambiar la contraseña");
        }
        return ResponseEntity.ok("Contraseña actualizada exitosamente");
    }
}

package com.redthread.identity.controller;

import com.redthread.identity.dto.UserProfileDto;
import com.redthread.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "Perfil del usuario autenticado")
@RestController
@RequestMapping("/me")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @Operation(summary = "Obtener mi perfil", description = "Devuelve id, nombre, email y roles desde el token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil OK",
                    content = @Content(schema = @Schema(implementation = UserProfileDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping
    public ResponseEntity<UserProfileDto> me() {
        return ResponseEntity.ok(users.getMyProfile());
    }
}

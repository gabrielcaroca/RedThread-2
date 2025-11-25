package com.redthread.identity.controller;

import com.redthread.identity.model.User;
import com.redthread.identity.model.Role;
import com.redthread.identity.repository.UserRepository;
import com.redthread.identity.repository.RoleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin", description = "Operaciones administrativas: usuarios y roles")
@RestController
@RequestMapping
public class AdminController {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    public AdminController(UserRepository userRepo, RoleRepository roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    @Operation(summary = "Listar usuarios")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuarios"),
            @ApiResponse(responseCode = "403", description = "Solo ADMIN")
    })
    @GetMapping("/users")
    public ResponseEntity<List<User>> listUsers() {
        return ResponseEntity.ok(userRepo.findAll());
    }

    @Operation(summary = "Obtener usuario por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe"),
            @ApiResponse(responseCode = "403", description = "Solo ADMIN")
    })
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Asignar rol a usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol asignado"),
            @ApiResponse(responseCode = "404", description = "Usuario o rol no existe"),
            @ApiResponse(responseCode = "403", description = "Solo ADMIN")
    })
    @PostMapping("/users/{id}/roles/{roleKey}")
    public ResponseEntity<User> addRole(@PathVariable Long id, @PathVariable String roleKey) {
        User u = userRepo.findById(id).orElse(null);
        Role r = roleRepo.findByKey(roleKey).orElse(null);
        if (u == null || r == null) return ResponseEntity.notFound().build();

        u.getRoles().add(r);
        return ResponseEntity.ok(userRepo.save(u));
    }

    @Operation(summary = "Listar roles")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de roles"),
            @ApiResponse(responseCode = "403", description = "Solo ADMIN")
    })
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> listRoles() {
        return ResponseEntity.ok(roleRepo.findAll());
    }
}

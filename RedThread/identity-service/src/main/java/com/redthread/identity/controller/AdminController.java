package com.redthread.identity.controller;

import com.redthread.identity.model.User;
import com.redthread.identity.model.Role;
import com.redthread.identity.repository.UserRepository;
import com.redthread.identity.repository.RoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class AdminController {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    public AdminController(UserRepository userRepo, RoleRepository roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    // GET /users
    @GetMapping("/users")
    public ResponseEntity<List<User>> listUsers() {
        return ResponseEntity.ok(userRepo.findAll());
    }

    // GET /users/{id}
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.of(userRepo.findById(id));
    }

    // POST /users/{id}/roles/{roleKey}
    @PostMapping("/users/{id}/roles/{roleKey}")
    public ResponseEntity<User> assignRole(@PathVariable Long id, @PathVariable String roleKey) {
        var user = userRepo.findById(id).orElseThrow();
        var role = roleRepo.findByKey(roleKey).orElseThrow();
        user.getRoles().add(role);
        userRepo.save(user);
        return ResponseEntity.ok(user);
    }

    // GET /roles
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> listRoles() {
        return ResponseEntity.ok(roleRepo.findAll());
    }
}

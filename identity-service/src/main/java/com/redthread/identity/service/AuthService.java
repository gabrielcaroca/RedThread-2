package com.redthread.identity.service;

import com.redthread.identity.dto.JwtResponse;
import com.redthread.identity.dto.LoginRequest;
import com.redthread.identity.dto.RegisterRequest;
import com.redthread.identity.model.Role;
import com.redthread.identity.model.User;
import com.redthread.identity.repository.RoleRepository;
import com.redthread.identity.repository.UserRepository;
import com.redthread.identity.security.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository userRepo, RoleRepository roleRepo, PasswordEncoder encoder, JwtService jwt) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public JwtResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email ya registrado");
        }

        Role roleCliente = roleRepo.findByKey("CLIENTE")
                .orElseThrow(() -> new IllegalStateException("Role CLIENTE no existe"));

        User u = new User();
        u.setEmail(req.getEmail().toLowerCase());
        u.setFullName(req.getFullName());
        u.setPassword(encoder.encode(req.getPassword())); // ← actualizado
        u.setRoles(Set.of(roleCliente));
        userRepo.save(u);

        String token = jwt.generate(u);
        return new JwtResponse(token, Instant.now().plusSeconds(60L * 60L * 2L)); // 2 horas
    }

    public JwtResponse login(LoginRequest req) {
        var u = userRepo.findByEmail(req.getEmail().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        System.out.println("Usando encoder: " + encoder.getClass().getSimpleName());
        System.out.println("Hash en BD: " + u.getPassword());
        System.out.println("Password ingresada: " + req.getPassword());

        if (!encoder.matches(req.getPassword(), u.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String token = jwt.generate(u);
        return new JwtResponse(token, Instant.now().plusSeconds(60L * 60L * 2L));
    }
}

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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository userRepo,
                       RoleRepository roleRepo,
                       PasswordEncoder encoder,
                       JwtService jwt) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public JwtResponse register(RegisterRequest req) {
        String email = req.getEmail().toLowerCase().trim();

        if (userRepo.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado");
        }

        Role defaultRole = roleRepo.findByKey("CLIENTE")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Rol CLIENTE no existe"
                ));

        User u = new User();
        u.setEmail(email);
        u.setFullName(req.getFullName().trim());
        u.setPassword(encoder.encode(req.getPassword()));
        u.setRoles(Set.of(defaultRole));

        u = userRepo.save(u);

        String token = jwt.generate(u);
        Instant exp = Instant.now().plusSeconds(60L * 60L * 2L); // 2h
        return new JwtResponse(token, exp);
    }

    public JwtResponse login(LoginRequest req) {
        String email = req.getEmail().toLowerCase().trim();

        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!encoder.matches(req.getPassword(), u.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        String token = jwt.generate(u);
        Instant exp = Instant.now().plusSeconds(60L * 60L * 2L); // 2h
        return new JwtResponse(token, exp);
    }

    public JwtResponse refresh(String token) {
        if (!jwt.isValid(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
        }

        Long userId = Long.valueOf(jwt.parse(token).getBody().getSubject());
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        String newToken = jwt.generate(u);
        Instant exp = Instant.now().plusSeconds(60L * 60L * 2L);
        return new JwtResponse(newToken, exp);
    }

    public void verify(String token) {
        if (!jwt.isValid(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
        }
    }
}

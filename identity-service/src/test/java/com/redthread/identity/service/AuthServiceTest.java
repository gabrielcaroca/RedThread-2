package com.redthread.identity.service;

import com.redthread.identity.dto.LoginRequest;
import com.redthread.identity.dto.RegisterRequest;
import com.redthread.identity.model.Role;
import com.redthread.identity.model.User;
import com.redthread.identity.repository.RoleRepository;
import com.redthread.identity.repository.UserRepository;
import com.redthread.identity.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepo;
    private RoleRepository roleRepo;
    private PasswordEncoder encoder;
    private JwtService jwt;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepository.class);
        roleRepo = mock(RoleRepository.class);
        encoder = mock(PasswordEncoder.class);
        jwt = mock(JwtService.class);
        authService = new AuthService(userRepo, roleRepo, encoder, jwt);
    }

    @Test
    void register_ok_createsUserAndReturnsToken() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@mail.com");
        req.setFullName("Test User");
        req.setPassword("1234");

        Role cliente = new Role();
        cliente.setKey("CLIENTE");
        cliente.setName("Cliente");

        when(userRepo.existsByEmail("test@mail.com")).thenReturn(false);
        when(roleRepo.findByKey("CLIENTE")).thenReturn(Optional.of(cliente));
        when(encoder.encode("1234")).thenReturn("HASH");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwt.generate(any(User.class))).thenReturn("TOKEN");

        var resp = authService.register(req);

        // JwtResponse real usa getAccessToken()
        assertEquals("TOKEN", resp.getAccessToken());

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(cap.capture());
        User saved = cap.getValue();

        assertEquals("test@mail.com", saved.getEmail());
        assertEquals("Test User", saved.getFullName());
        assertEquals("HASH", saved.getPassword());
        assertTrue(saved.getRoles().stream().anyMatch(r -> r.getKey().equals("CLIENTE")));
    }

    @Test
    void register_duplicateEmail_throws409() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("dup@mail.com");
        req.setFullName("Dup");
        req.setPassword("1234");

        when(userRepo.existsByEmail("dup@mail.com")).thenReturn(true);

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> authService.register(req));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void login_ok_returnsToken() {
        LoginRequest req = new LoginRequest();
        req.setEmail("a@mail.com");
        req.setPassword("pass");

        User u = new User();
        u.setEmail("a@mail.com");
        u.setPassword("HASH");
        u.setRoles(Set.of());

        when(userRepo.findByEmail("a@mail.com")).thenReturn(Optional.of(u));
        when(encoder.matches("pass", "HASH")).thenReturn(true);
        when(jwt.generate(u)).thenReturn("TOKEN");

        var resp = authService.login(req);

        assertEquals("TOKEN", resp.getAccessToken());
    }

    @Test
    void login_invalidPassword_throws401() {
        LoginRequest req = new LoginRequest();
        req.setEmail("a@mail.com");
        req.setPassword("bad");

        User u = new User();
        u.setEmail("a@mail.com");
        u.setPassword("HASH");

        when(userRepo.findByEmail("a@mail.com")).thenReturn(Optional.of(u));
        when(encoder.matches("bad", "HASH")).thenReturn(false);

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> authService.login(req));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }
}

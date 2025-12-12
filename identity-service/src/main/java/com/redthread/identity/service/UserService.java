package com.redthread.identity.service;

import com.redthread.identity.dto.ChangePasswordRequest;
import com.redthread.identity.dto.UpdateMeRequest;
import com.redthread.identity.dto.UserListDto;
import com.redthread.identity.dto.UserProfileDto;
import com.redthread.identity.model.User;
import com.redthread.identity.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    // ✅ Necesario para AddressService (y para cualquier cosa que necesite el User entity actual)
    public User getCurrentUserEntity() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User u) {
            return u;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
    }

    public UserProfileDto getMyProfile() {
        User u = getCurrentUserEntity();

        return new UserProfileDto(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getRoles().stream().map(r -> r.getKey()).collect(Collectors.toSet())
        );
    }

    public UserProfileDto updateMyProfile(UpdateMeRequest req) {
        User u = getCurrentUserEntity();

        String newEmail = req.getEmail().toLowerCase().trim();
        String newName = req.getFullName().trim();

        if (!newEmail.equalsIgnoreCase(u.getEmail())) {
            if (repo.existsByEmail(newEmail)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ese correo ya está en uso");
            }
            u.setEmail(newEmail);
        }

        u.setFullName(newName);

        User saved = repo.save(u);

        return new UserProfileDto(
                saved.getId(),
                saved.getFullName(),
                saved.getEmail(),
                saved.getRoles().stream().map(r -> r.getKey()).collect(Collectors.toSet())
        );
    }

    public void changeMyPassword(ChangePasswordRequest req) {
        User u = getCurrentUserEntity();

        if (!encoder.matches(req.getCurrentPassword(), u.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contraseña actual incorrecta");
        }

        u.setPassword(encoder.encode(req.getNewPassword()));
        repo.save(u);
    }

    // ============================
    // Admin stuff (tal cual estaba)
    // ============================
    public List<UserListDto> getAllUsers() {
        return repo.findAll().stream().map(u ->
                new UserListDto(
                        u.getId(),
                        u.getFullName(),
                        u.getEmail(),
                        u.getCreatedAt(),
                        u.getRoles().stream()
                                .map(r -> r.getKey())
                                .collect(java.util.stream.Collectors.toSet())
                )
        ).toList();
    }
}

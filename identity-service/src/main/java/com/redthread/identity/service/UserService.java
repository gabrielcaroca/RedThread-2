package com.redthread.identity.service;

import com.redthread.identity.dto.UserListDto;
import com.redthread.identity.dto.UserProfileDto;
import com.redthread.identity.model.User;
import com.redthread.identity.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public User getCurrentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User)) {
            throw new IllegalStateException("No autenticado");
        }
        return (User) auth.getPrincipal();
    }

    public UserProfileDto getMyProfile() {
        User u = getCurrentUserEntity();
        var roles = u.getRoles().stream().map(r -> r.getKey()).collect(java.util.stream.Collectors.toSet());
        return new UserProfileDto(u.getId(), u.getFullName(), u.getEmail(), roles);
    }

    // 🔥 NUEVO → obtener todos los usuarios
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

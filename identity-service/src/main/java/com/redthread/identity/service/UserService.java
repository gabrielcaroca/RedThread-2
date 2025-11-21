package com.redthread.identity.service;

import com.redthread.identity.dto.UserProfileDto;
import com.redthread.identity.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class UserService {
    public User getCurrentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User)) {
            throw new IllegalStateException("No autenticado");
        }
        return (User) auth.getPrincipal();
    }

    public UserProfileDto getMyProfile() {
        User u = getCurrentUserEntity();
        var roles = u.getRoles().stream().map(r -> r.getKey()).collect(Collectors.toSet());
        return new UserProfileDto(u.getId(), u.getFullName(), u.getEmail(), roles);
    }
}

package com.redthread.identity.dto;

import java.time.Instant;
import java.util.Set;

public record UserListDto(
        Long id,
        String fullName,
        String email,
        Instant createdAt,
        Set<String> roles
) {}
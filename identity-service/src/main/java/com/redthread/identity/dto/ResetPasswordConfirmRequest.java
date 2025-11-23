package com.redthread.identity.dto;

public record ResetPasswordConfirmRequest(
        String identifier,
        String newPassword
) {}

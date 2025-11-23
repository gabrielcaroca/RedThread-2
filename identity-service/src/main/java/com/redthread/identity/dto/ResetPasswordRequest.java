package com.redthread.identity.dto;

public record ResetPasswordRequest(
        String identifier   // puede ser email o username, como tu controller lo quiere
) {}

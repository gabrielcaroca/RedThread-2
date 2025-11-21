package com.redthread.identity.dto;

import java.time.Instant;

public class JwtResponse {
    private String tokenType = "Bearer";
    private String accessToken;
    private Instant expiresAt;
    public JwtResponse(String accessToken, Instant expiresAt) {
        this.accessToken = accessToken; this.expiresAt = expiresAt;
    }
    public String getTokenType() { return tokenType; }
    public String getAccessToken() { return accessToken; }
    public Instant getExpiresAt() { return expiresAt; }
}

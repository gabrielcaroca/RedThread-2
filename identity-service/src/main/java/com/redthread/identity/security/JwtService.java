package com.redthread.identity.security;

import com.redthread.identity.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets; // <-- IMPORTACIÓN AÑADIDA
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class JwtService {
    @Value("${security.jwt.secret}") private String secret;
    @Value("${security.jwt.issuer}") private String issuer;
    @Value("${security.jwt.expiry-minutes}") private long expiryMinutes;

    private Key key;

    @PostConstruct
    void init() {
        // LÍNEA MODIFICADA para usar UTF-8
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generate(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(expiryMinutes, ChronoUnit.MINUTES);
        String roles = user.getRoles().stream().map(r -> r.getKey()).collect(Collectors.joining(","));
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("email", user.getEmail())
                .claim("name", user.getFullName())
                .claim("roles", roles)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Key getKey() { return key; }
}
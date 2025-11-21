package com.redthread.order.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

  @Value("${app.security.jwt.secret:}")
  private String secretFromYml;

  private byte[] resolveSecret() {
    String raw = (secretFromYml != null && !secretFromYml.isBlank())
        ? secretFromYml
        : System.getenv().getOrDefault("JWT_SECRET", "");
    if (raw.isBlank()) throw new IllegalStateException("JWT secret no configurado (app.security.jwt.secret o env JWT_SECRET)");
    return raw.getBytes(StandardCharsets.UTF_8);
  }

  @Bean
public JwtDecoder jwtDecoder() {
    byte[] keyBytes = resolveSecret();
    SecretKeySpec key = new SecretKeySpec(keyBytes, "HmacSHA256");
    return NimbusJwtDecoder.withSecretKey(key)
        .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256)
        .build();
}


  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable());
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
        .anyRequest().authenticated()
    );
    http.oauth2ResourceServer(oauth -> oauth
        .jwt(Customizer.withDefaults())
        .authenticationEntryPoint((req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED)));
    return http.build();
  }
}

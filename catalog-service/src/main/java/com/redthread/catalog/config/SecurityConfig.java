package com.redthread.catalog.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    @Value("${app.security.jwt.secret:${JWT_SECRET:}}")
    private String jwtSecret;

    private SecretKey secretKey;

    @PostConstruct
    void init() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("JWT secret no configurado.");
        }
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())

            // ============================
            // Resource Server (JWT)
            // ============================
            .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))

            // ============================
            // Authorization Rules
            // ============================
            .authorizeHttpRequests(auth -> auth

                // ----------- RUTAS PÚBLICAS -----------
                // imágenes
                .requestMatchers("/media/**").permitAll()

                // catálogo público
                .requestMatchers("/products/**").permitAll()
                .requestMatchers("/categories/**").permitAll()
                .requestMatchers("/brands/**").permitAll()

                // variants GET = público
                .requestMatchers("/variants/**").permitAll()

                // ----------- RUTAS PROTEGIDAS (solo POST/PUT/DELETE) -----------
                .requestMatchers("/variants").authenticated()
                .requestMatchers("/products").authenticated()

                // cualquier otra ruta = requiere token
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(this.secretKey).build();
        decoder.setJwtValidator(JwtValidators.createDefault());
        return decoder;
    }

    // ============================
    // CORS
    // ============================
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:5173",
                                "http://localhost:3000",
                                "http://127.0.0.1:3000",
                                "http://10.0.2.2:3000",
                                "*"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false);
            }
        };
    }
}

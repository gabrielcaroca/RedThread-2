package com.redthread.identity.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "RedThread Identity Service API",
                version = "1.0.0",
                description = "Microservicio de autenticación, usuarios, roles y direcciones.",
                contact = @Contact(name = "RedThread"),
                license = @License(name = "Apache-2.0")
        )
)
public class OpenApiConfig { }

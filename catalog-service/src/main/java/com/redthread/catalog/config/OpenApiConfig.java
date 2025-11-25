package com.redthread.catalog.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "RedThread Catalog Service API",
                version = "1.0.0",
                description = "Microservicio de catálogo: productos, variantes, imágenes y stock.",
                contact = @Contact(name = "RedThread Team"),
                license = @License(name = "Apache 2.0")
        ),
        servers = {
                @Server(url = "http://localhost:8082", description = "Local")
        }
)
public class OpenApiConfig { }

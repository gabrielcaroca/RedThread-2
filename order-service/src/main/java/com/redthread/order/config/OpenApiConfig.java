package com.redthread.order.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "RedThread Order Service API",
        version = "v1",
        description = "Microservicio de carrito, direcciones, checkout y ordenes."
    ),
    servers = {
        @Server(url = "http://localhost:8083", description = "Local")
    }
)
public class OpenApiConfig {}

package com.redthread.delivery.config;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * NOTA: En MVC tradicional, propagaremos el token manualmente desde el servicio.
 * Este filtro es útil si luego migras a WebFlux end-to-end.
 */
@Component
public class BearerPropagationFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange);
    }
}

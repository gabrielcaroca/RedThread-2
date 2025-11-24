package com.redthread.delivery.integration;

import com.redthread.delivery.integration.dto.OrderDeliveryResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class OrderClient {

    private final WebClient orderWebClient;

    public OrderClient(WebClient orderWebClient) {
        this.orderWebClient = orderWebClient;
    }

    // Devuelve el pedido completo (lo usa ShipmentServiceImpl)
    public Mono<Map<String, Object>> getOrderById(Long orderId, Jwt jwt) {
        String bearer = jwt != null ? jwt.getTokenValue() : null;

        return orderWebClient
                .get()
                .uri("/orders/{id}", orderId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorResume(ex -> Mono.empty());
    }

    // DEVUELVE { userId, shippingAddress } desde order-service
    // endpoint real que tú definiste: GET /orders/{id}/delivery
    public Mono<OrderDeliveryResponse> getDeliveryInfo(Long orderId, Jwt jwt) {
        String bearer = jwt != null ? jwt.getTokenValue() : null;

        return orderWebClient
                .get()
                .uri("/orders/{id}/delivery", orderId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer)
                .retrieve()
                .bodyToMono(OrderDeliveryResponse.class)
                .onErrorResume(ex -> Mono.empty());
    }

    public Mono<Void> postDeliveryStatus(Long orderId, String status, String note, Jwt jwt) {
        String bearer = jwt != null ? jwt.getTokenValue() : null;
        Map<String, Object> body = Map.of("status", status, "note", note);

        return orderWebClient
                .post()
                .uri("/orders/{id}/delivery-status", orderId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(ex -> Mono.empty());
    }
}

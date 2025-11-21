package com.redthread.order.integrations;

import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CatalogClient {

  private final WebClient catalogWebClient;

  private String currentToken() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
      return jwt.getTokenValue();
    }
    return null;
  }

  public record VariantInfo(Long id, BigDecimal price, Integer availableStock) {}

  public VariantInfo findVariantById(Long variantId) {
    String token = currentToken();
    try {
      Map<String, Object> m = catalogWebClient.get()
          .uri("/inventory/by-variant/{id}", variantId)
          .headers(h -> { if (token != null) h.setBearerAuth(token); })
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .bodyToMono(Map.class)
          .block();

      if (m == null) return null;

      Map<String, Object> variant = (Map<String, Object>) m.get("variant");
      if (variant == null) return null;

      BigDecimal price = BigDecimal.ZERO;

      if (variant.containsKey("priceOverride") && variant.get("priceOverride") != null) {
        price = new BigDecimal(String.valueOf(variant.get("priceOverride")));
      } else if (variant.containsKey("product")) {
        Map<String, Object> product = (Map<String, Object>) variant.get("product");
        if (product != null && product.get("basePrice") != null) {
          price = new BigDecimal(String.valueOf(product.get("basePrice")));
        }
      }

      Integer stock = 0;
      if (m.get("stockAvailable") != null) {
        stock = Integer.parseInt(String.valueOf(m.get("stockAvailable")));
      }

      return new VariantInfo(variantId, price, stock);

    } catch (Exception ex) {
      System.err.println(" Error al obtener variant desde Catalog-Service: " + ex.getMessage());
      return null;
    }
  }

  
  public void adjustStock(Long variantId, int delta) {
    String token = currentToken();
    catalogWebClient.post()
        .uri("/inventory/adjust")
        .headers(h -> { if (token != null) h.setBearerAuth(token); })
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(Map.of("variantId", variantId, "delta", delta))
        .retrieve()
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            resp -> resp.bodyToMono(String.class)
                .flatMap(msg -> Mono.error(new IllegalStateException("Catalog adjust error: " + msg))))
        .toBodilessEntity()
        .block();
  }
}

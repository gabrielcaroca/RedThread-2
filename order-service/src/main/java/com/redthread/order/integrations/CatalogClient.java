package com.redthread.order.integrations;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.redthread.order.dto.VariantAdminInfo;

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
      // ==========================
      // 1) Obtener la variante
      // ==========================
      Map<String, Object> variant = catalogWebClient.get()
          .uri("/variants/{id}", variantId)
          .headers(h -> { if (token != null) h.setBearerAuth(token); })
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .bodyToMono(Map.class)
          .block();

      if (variant == null) {
        return null;
      }

      // ==========================
      // 2) Resolver precio
      // ==========================
      BigDecimal price = BigDecimal.ZERO;

      Object overrideObj = variant.get("priceOverride");
      if (overrideObj != null) {
        price = new BigDecimal(String.valueOf(overrideObj));
      } else {
        // Fallback al basePrice del producto
        Object productIdObj = variant.get("productId");
        if (productIdObj != null) {
          Long productId = Long.parseLong(String.valueOf(productIdObj));

          Map<String, Object> product = catalogWebClient.get()
              .uri("/products/{id}", productId)
              .headers(h -> { if (token != null) h.setBearerAuth(token); })
              .accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .bodyToMono(Map.class)
              .block();

          if (product != null && product.get("basePrice") != null) {
            price = new BigDecimal(String.valueOf(product.get("basePrice")));
          }
        }
      }

      // ==========================
      // 3) Stock desde inventario
      // ==========================
      Integer stock = 0;

      Map<String, Object> inventory = catalogWebClient.get()
          .uri("/inventory/by-variant/{id}", variantId)
          .headers(h -> { if (token != null) h.setBearerAuth(token); })
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .bodyToMono(Map.class)
          .onErrorResume(ex -> Mono.empty())
          .block();

      if (inventory != null) {
        int available = 0;
        int reserved = 0;

        Object stockAvailable = inventory.get("stockAvailable");
        Object stockReserved = inventory.get("stockReserved");

        if (stockAvailable != null) {
          available = Integer.parseInt(String.valueOf(stockAvailable));
        }
        if (stockReserved != null) {
          reserved = Integer.parseInt(String.valueOf(stockReserved));
        }

        stock = Math.max(available - reserved, 0);
      }

      return new VariantInfo(variantId, price, stock);

    } catch (Exception ex) {
      System.err.println("Error al obtener variant desde Catalog-Service: " + ex.getMessage());
      return null;
    }
  }

public VariantAdminInfo getVariantAdmin(Long variantId) {

  String token = currentToken();

  Map<String, Object> variant = catalogWebClient.get()
      .uri("/variants/{id}", variantId)
      .headers(h -> { if (token != null) h.setBearerAuth(token); })
      .retrieve()
      .bodyToMono(Map.class)
      .block();

  if (variant == null) return null;

  Map<String, Object> product = catalogWebClient.get()
      .uri("/products/{id}", variant.get("productId"))
      .headers(h -> { if (token != null) h.setBearerAuth(token); })
      .retrieve()
      .bodyToMono(Map.class)
      .block();

  return new VariantAdminInfo(
      variantId,
      product.get("name").toString(),
      variant.get("sizeValue").toString(),
      variant.get("color").toString()
  );
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

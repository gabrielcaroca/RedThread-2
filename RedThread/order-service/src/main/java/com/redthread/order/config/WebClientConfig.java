package com.redthread.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;

@Configuration
public class WebClientConfig {

  @Value("${app.catalog.base-url}")
  private String catalogBaseUrl;

  @Value("${app.http.connect-timeout-ms:3000}")
  private int connectTimeoutMs;

  @Value("${app.http.read-timeout-ms:5000}")
  private int readTimeoutMs;

  @Bean
  public WebClient catalogWebClient() {
    HttpClient httpClient = HttpClient.create()
        .responseTimeout(Duration.ofMillis(readTimeoutMs));

    return WebClient.builder()
        .baseUrl(catalogBaseUrl)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .exchangeStrategies(ExchangeStrategies.builder()
            .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build())
        .build();
  }
}

package com.redthread.delivery.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.*;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Value("${app.order.base-url}")
    private String orderBaseUrl;

    @Value("${app.catalog.base-url}")
    private String catalogBaseUrl;

    @Value("${app.http.connect-timeout-ms:4000}")
    private int connectTimeoutMs;

    @Value("${app.http.read-timeout-ms:8000}")
    private int readTimeoutMs;

    @Bean
    public WebClient orderWebClient(ExchangeStrategies strategies) {
        return baseClient(orderBaseUrl, strategies);
    }

    @Bean
    public WebClient catalogWebClient(ExchangeStrategies strategies) {
        return baseClient(catalogBaseUrl, strategies);
    }

    @Bean
    public ExchangeStrategies exchangeStrategies() {
        return ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }

    private WebClient baseClient(String baseUrl, ExchangeStrategies strategies) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofMillis(readTimeoutMs))
                .doOnConnected(
                        conn -> conn.addHandlerLast(new ReadTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .filter(ExchangeFilterFunctions.statusError(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> WebClientResponseException.create(
                                resp.rawStatusCode(),
                                resp.statusCode().toString(), // ← antes: getReasonPhrase()
                                resp.headers().asHttpHeaders(),
                                null,
                                null)))
                .build();
    }
}

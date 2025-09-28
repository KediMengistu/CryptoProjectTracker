// src/main/java/com/example/backend/config/HttpClientConfig.java
package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HttpClientConfig {

    @Bean
    public WebClient coinGeckoWebClient(CoinGeckoProperties props) {
        if (props.getBaseUrl() == null || props.getBaseUrl().isBlank()) {
            throw new IllegalStateException("coingecko.base-url must be set");
        }
        WebClient.Builder b = WebClient.builder().baseUrl(props.getBaseUrl());
        if (props.getApiKey() != null && !props.getApiKey().isBlank()) {
            b.defaultHeader("x-cg-demo-api-key", props.getApiKey());
        }
        return b.build();
    }
}

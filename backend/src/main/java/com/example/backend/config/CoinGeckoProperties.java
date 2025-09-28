// src/main/java/com/example/backend/config/CoinGeckoProperties.java
package com.example.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "coingecko")
public class CoinGeckoProperties {
    private String baseUrl;  // required
    private String apiKey;   // optional

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
}

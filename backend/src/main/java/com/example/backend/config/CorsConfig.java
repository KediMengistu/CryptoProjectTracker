package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
public class CorsConfig {

    /**
     * Comma-separated list of allowed origin patterns.
     * This value should come from the env var APP_CORS_ALLOWED_ORIGINS.
     */
    @Bean
    public CorsWebFilter corsWebFilter(
            @Value("${app.cors.allowed-origins:}") String allowedOriginsProp
    ) {
        // Parse CSV -> ordered unique set
        Set<String> patterns = Arrays.stream(allowedOriginsProp.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(new ArrayList<>(patterns));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        // Keep false unless you send cookies/Authorization from the browser
        cfg.setAllowCredentials(false);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return new CorsWebFilter(source);
    }
}

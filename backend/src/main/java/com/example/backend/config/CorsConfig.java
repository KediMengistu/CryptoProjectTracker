package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration cfg = new CorsConfiguration();

        // In local dev your frontend origin is Next.js on :3000
        cfg.setAllowedOrigins(List.of("http://localhost:3000"));

        // Allow typical methods used by your app
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allow any headers your frontend may send (Accept, Content-Type, etc.)
        cfg.setAllowedHeaders(List.of("*"));

        // Only set true if you actually send cookies/Authorization from browser to backend
        cfg.setAllowCredentials(false);

        // Cache preflight for 1 hour
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return new CorsWebFilter(source);
    }
}

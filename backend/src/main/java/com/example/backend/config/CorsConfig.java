package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration cfg = new CorsConfiguration();

        // Allow local dev + your deployed Vercel URL + all preview Vercel URLs
        cfg.setAllowedOriginPatterns(List.of(
            "http://localhost:3000",
            "https://crypto-project-tracker-fz0toyhp0-kedimengistus-projects.vercel.app",
            "https://*.vercel.app"
        ));

        // Typical methods your app uses
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allow any headers your frontend may send (Accept, Content-Type, etc.)
        cfg.setAllowedHeaders(List.of("*"));

        // Keep false if you’re not sending cookies/Authorization from the browser
        cfg.setAllowCredentials(false);

        // Cache preflight for 1 hour
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return new CorsWebFilter(source);
    }
}

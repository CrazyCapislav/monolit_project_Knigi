package dev.petr.bookswap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS configuration for frontend integration.
 * 
 * Allows:
 * - Requests from frontend (default: http://localhost:3000)
 * - Custom headers: X-User-Id (authentication), X-Total-Count (pagination)
 * - All standard HTTP methods
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);

        for (String origin : allowedOrigins.split(",")) {
            config.addAllowedOrigin(origin.trim());
        }

        config.addAllowedHeader("*");

        config.setExposedHeaders(List.of(
                "X-Total-Count",
                "Content-Type",
                "Authorization"
        ));

        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}


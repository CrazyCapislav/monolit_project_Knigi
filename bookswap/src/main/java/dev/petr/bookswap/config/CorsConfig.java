package dev.petr.bookswap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS конфигурация для взаимодействия с frontend.
 * 
 * Разрешает:
 * - Запросы с frontend (по умолчанию http://localhost:3000)
 * - Кастомные заголовки: X-User-Id (аутентификация), X-Total-Count (пагинация)
 * - Все стандартные HTTP методы
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Разрешить credentials (cookies, authorization headers)
        config.setAllowCredentials(true);

        // Разрешенные источники (frontend URLs)
        // Можно указать несколько через запятую в переменной окружения
        for (String origin : allowedOrigins.split(",")) {
            config.addAllowedOrigin(origin.trim());
        }

        // Разрешить все стандартные заголовки
        config.addAllowedHeader("*");

        // Разрешить кастомные заголовки для нашего API
        config.setExposedHeaders(List.of(
                "X-Total-Count",     // Для пагинации с общим количеством
                "Content-Type",
                "Authorization"
        ));

        // Разрешить все HTTP методы
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        // Применить конфигурацию ко всем путям API
        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}


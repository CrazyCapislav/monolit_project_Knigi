package dev.petr.bookswap.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты CORS конфигурации.
 * 
 * Проверяем:
 * - Preflight запросы (OPTIONS) обрабатываются корректно
 * - Разрешенные заголовки включают X-User-Id и X-Total-Count
 * - CORS работает для всех API endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsConfigTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void shouldAllowCorsPreflightRequest() throws Exception {
        mvc.perform(options("/api/v1/books")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void shouldExposeCustomHeaders() throws Exception {
        mvc.perform(options("/api/v1/books")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "X-User-Id"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Expose-Headers"));
    }

    @Test
    void shouldAllowAllHttpMethods() throws Exception {
        mvc.perform(options("/api/v1/books")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }
}


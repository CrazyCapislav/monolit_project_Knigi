package dev.petr.bookswap.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Базовый класс для интеграционных тестов.
 * 
 * Использует существующий контейнер PostgreSQL из docker-compose.
 * Перед запуском тестов убедитесь, что docker-compose запущен:
 *   docker-compose up -d db
 * 
 * Настройки подключения к БД берутся из application-test.yml:
 *   spring.datasource.url=jdbc:postgresql://localhost:5432/books
 *   spring.datasource.username=books
 *   spring.datasource.password=books
 * 
 * Это решение используется из-за проблем с подключением testcontainers к Docker на Windows.
 * Для изолированных тестов рекомендуется настроить testcontainers и использовать
 * контейнеры, создаваемые автоматически.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {
    // Используем существующий контейнер из docker-compose
    // Настройки подключения берутся из application-test.yml
}


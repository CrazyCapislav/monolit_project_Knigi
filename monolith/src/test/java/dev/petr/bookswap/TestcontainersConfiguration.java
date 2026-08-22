package dev.petr.bookswap;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Provides a disposable PostgreSQL instance for tests that need a real database.
 *
 * Previously the test profile pointed at a fixed localhost:5432 database that
 * had to be started by hand with docker-compose. That made the suite depend on
 * the developer's machine state and impossible to run on CI. {@code @ServiceConnection}
 * lets Spring Boot derive the datasource from the container instead.
 *
 * The container is static so all test classes share one instance rather than
 * starting a database per class.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"));

    static {
        POSTGRES.start();
    }

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return POSTGRES;
    }
}

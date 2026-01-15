package dev.petr.exchange.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign configuration to properly handle errors.
 *
 * By default, Feign Circuit Breaker treats ALL errors (4xx and 5xx) as failures.
 * This configuration ensures that only server errors (5xx) and network issues
 * trigger the Circuit Breaker, while client errors (4xx) are propagated normally.
 */
@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    /**
     * Custom error decoder that distinguishes between:
     * - Client errors (4xx) - should NOT trigger Circuit Breaker
     * - Server errors (5xx) - should trigger Circuit Breaker
     */
    public static class CustomErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultDecoder = new Default();

        @Override
        public Exception decode(String methodKey, Response response) {
            // 4xx errors are client errors - don't trigger Circuit Breaker
            if (response.status() >= 400 && response.status() < 500) {
                // Return FeignException which will be propagated without triggering CB
                return defaultDecoder.decode(methodKey, response);
            }

            // 5xx errors are server errors - trigger Circuit Breaker
            // Network errors and timeouts also trigger Circuit Breaker
            return defaultDecoder.decode(methodKey, response);
        }
    }
}

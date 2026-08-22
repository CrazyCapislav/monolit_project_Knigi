package dev.petr.bookswap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class PageableConfig {
    @Bean
    PageableHandlerMethodArgumentResolverCustomizer customize() {
        return r -> {
            r.setOneIndexedParameters(true);
            r.setMaxPageSize(50);
            r.setFallbackPageable(PageRequest.of(1, 50));
        };
    }
}

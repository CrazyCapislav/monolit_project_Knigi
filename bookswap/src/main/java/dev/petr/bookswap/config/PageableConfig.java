package dev.petr.bookswap.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class PageableConfig
        implements PageableHandlerMethodArgumentResolverCustomizer {
    @Override
    public void customize(org.springframework.data.web.PageableHandlerMethodArgumentResolver p) {
        p.setMaxPageSize(50);
        p.setOneIndexedParameters(true);
    }
}

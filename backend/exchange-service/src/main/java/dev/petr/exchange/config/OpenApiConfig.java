package dev.petr.exchange.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${gateway.url:http://localhost:8080}")
    private String gatewayUrl;

    @Bean
    public OpenAPI exchangeServiceOpenAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url(gatewayUrl).description("API Gateway")))
                .info(new Info()
                        .title("Exchange Service API")
                        .description("Book exchange management service for BookSwap")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BookSwap Team")
                                .email("support@bookswap.dev")));
    }

    @Bean
    public GroupedOpenApi exchangeApi() {
        return GroupedOpenApi.builder()
                .group("exchange")
                .pathsToMatch("/api/v1/exchanges/**")
                .build();
    }
}
package dev.petr.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BookSwap API Gateway")
                        .description("Unified API documentation for all BookSwap microservices")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BookSwap Team")
                                .email("support@bookswap.dev")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Gateway Server")
                ));
    }

}



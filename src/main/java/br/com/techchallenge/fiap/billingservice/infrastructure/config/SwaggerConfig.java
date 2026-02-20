package br.com.techchallenge.fiap.billingservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI configuration for API documentation.
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.servlet.context-path:/api/billing-service}")
    private String contextPath;

    @Bean
    public OpenAPI billingServiceOpenAPI() {
        Server server = new Server()
                .url(contextPath)
                .description("Current Server");

        return new OpenAPI()
                .info(new Info()
                        .title("Billing Service API")
                        .description("Microsserviço de Orçamento e Pagamento - CarGarage")
                        .version("0.2.0")
                        .contact(new Contact()
                                .name("Leonardo Almeida")
                                .email("leonardo@example.com"))
                        .license(new License()
                                .name("Tech Challenge FIAP")
                                .url("https://fiap.com.br")))
                .servers(List.of(server));
    }
}

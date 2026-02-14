package br.com.techchallenge.fiap.billingservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration for API documentation.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI billingServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Billing Service API")
                .description("Microsserviço de Orçamento e Pagamento - CarGarage")
                .version("0.1.0")
                .contact(new Contact()
                    .name("Leonardo Almeida")
                    .email("leonardo@example.com"))
                .license(new License()
                    .name("Tech Challenge FIAP")
                    .url("https://fiap.com.br")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT token obtido via lambda-cargarage-auth")));
    }
}

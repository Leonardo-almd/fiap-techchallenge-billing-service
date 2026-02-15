package br.com.techchallenge.fiap.billingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test for main application class.
 * Verifies that Spring Boot context loads successfully.
 * Excludes Security/OAuth2 so context loads without JWK/issuer in test.
 */
@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
    SecurityAutoConfiguration.class,
    OAuth2ResourceServerAutoConfiguration.class
})
class BillingServiceApplicationTest {

    @Test
    void contextLoads() {
        // Verifica se o contexto Spring Boot carrega sem erros
    }
}

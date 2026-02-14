package br.com.techchallenge.fiap.billingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test for main application class.
 * Verifies that Spring Boot context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class BillingServiceApplicationTest {

    @Test
    void contextLoads() {
        // Verifica se o contexto Spring Boot carrega sem erros
    }
}

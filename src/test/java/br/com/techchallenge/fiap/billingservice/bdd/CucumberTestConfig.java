package br.com.techchallenge.fiap.billingservice.bdd;

import org.springframework.context.annotation.Configuration;

/**
 * Minimal Spring configuration for Cucumber BDD tests.
 * Step definitions create their own use cases and mocks; this context only satisfies
 * cucumber-spring's requirement for a context configuration.
 */
@Configuration
public class CucumberTestConfig {
}

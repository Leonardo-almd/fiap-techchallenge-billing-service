package br.com.techchallenge.fiap.billingservice.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;

/**
 * Spring context configuration for Cucumber BDD tests.
 * Required by cucumber-spring so that step definitions can run with a Spring context.
 * Uses plain @ContextConfiguration (no Spring Boot) to avoid loading AWS/DynamoDB/SQS.
 */
@CucumberContextConfiguration
@ContextConfiguration(classes = CucumberTestConfig.class)
public class CucumberSpringConfiguration {
}

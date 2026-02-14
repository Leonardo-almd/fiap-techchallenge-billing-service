package br.com.techchallenge.fiap.billingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Billing Service Application.
 * 
 * Microsserviço responsável por:
 * - Geração de orçamentos
 * - Aprovação/rejeição de orçamentos
 * - Processamento de pagamentos
 * - Participação no Saga Pattern para coordenação distribuída
 * 
 * Arquitetura: Clean Architecture
 * - application: Core (casos de uso, entidades, portas)
 * - infrastructure: Adaptadores (controllers, repositories, mensageria)
 */
@SpringBootApplication
@EnableScheduling
public class BillingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingServiceApplication.class, args);
    }
}

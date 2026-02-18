package br.com.techchallenge.fiap.billingservice.infrastructure.config;

import br.com.techchallenge.fiap.billingservice.application.gateway.ExternalPaymentProvider;
import br.com.techchallenge.fiap.billingservice.infrastructure.payment.MercadoPagoPaymentProvider;
import br.com.techchallenge.fiap.billingservice.infrastructure.payment.PaymentGatewaySimulator;
import br.com.techchallenge.fiap.billingservice.infrastructure.payment.PaymentGatewaySimulatorAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the active external payment provider: Mercado Pago (production) or simulator (local/test).
 */
@Configuration
public class PaymentProviderConfig {

    @Bean
    public ExternalPaymentProvider externalPaymentProvider(
            @Value("${mercadopago.enabled:false}") boolean mercadopagoEnabled,
            @Value("${mercadopago.payer-email:test_user_br@testuser.com}") String payerEmail,
            PaymentGatewaySimulator simulator) {
        if (mercadopagoEnabled) {
            return new MercadoPagoPaymentProvider(payerEmail);
        }
        return new PaymentGatewaySimulatorAdapter(simulator);
    }
}

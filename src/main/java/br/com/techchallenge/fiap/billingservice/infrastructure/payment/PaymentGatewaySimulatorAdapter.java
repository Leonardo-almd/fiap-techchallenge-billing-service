package br.com.techchallenge.fiap.billingservice.infrastructure.payment;

import br.com.techchallenge.fiap.billingservice.application.gateway.ExternalPaymentProvider;
import br.com.techchallenge.fiap.billingservice.application.gateway.PaymentProviderRequest;
import br.com.techchallenge.fiap.billingservice.application.gateway.PaymentProviderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
/**
 * Adapter that delegates to {@link PaymentGatewaySimulator} implementing {@link ExternalPaymentProvider}.
 * Used when Mercado Pago is disabled (e.g. local/test profiles).
 */
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewaySimulatorAdapter implements ExternalPaymentProvider {

    private final PaymentGatewaySimulator simulator;

    @Override
    public PaymentProviderResult processPayment(PaymentProviderRequest request) {
        PaymentGatewaySimulator.PaymentRequest simRequest =
                PaymentGatewaySimulator.PaymentRequest.builder()
                        .amount(request.amount())
                        .method(request.method())
                        .build();
        PaymentGatewaySimulator.PaymentResult simResult = simulator.processPayment(simRequest);
        return mapResult(simResult);
    }

    private static PaymentProviderResult mapResult(PaymentGatewaySimulator.PaymentResult simResult) {
        if (simResult.success()) {
            return PaymentProviderResult.success(simResult.externalId(), simResult.authorizationCode());
        }
        return PaymentProviderResult.failure(simResult.failureReason());
    }
}

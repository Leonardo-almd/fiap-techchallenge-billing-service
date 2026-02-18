package br.com.techchallenge.fiap.billingservice.application.gateway;

/**
 * Port for processing payments with an external payment provider (e.g. Mercado Pago).
 * Implementations can be the real Mercado Pago integration or a simulator for tests.
 */
public interface ExternalPaymentProvider {

    /**
     * Process a payment with the external provider.
     *
     * @param request payment details (amount, method, description, external reference)
     * @return result with success/failure, external id, authorization code or failure reason
     */
    PaymentProviderResult processPayment(PaymentProviderRequest request);
}

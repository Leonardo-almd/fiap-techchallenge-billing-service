package br.com.techchallenge.fiap.billingservice.application.gateway;

import lombok.Builder;

/**
 * Result of processing a payment with an external provider (e.g. Mercado Pago or simulator).
 */
@Builder
public record PaymentProviderResult(
    boolean success,
    String externalId,
    String authorizationCode,
    String failureReason
) {
    public static PaymentProviderResult success(String externalId, String authorizationCode) {
        return new PaymentProviderResult(true, externalId, authorizationCode, null);
    }

    public static PaymentProviderResult failure(String failureReason) {
        return new PaymentProviderResult(false, null, null, failureReason);
    }
}

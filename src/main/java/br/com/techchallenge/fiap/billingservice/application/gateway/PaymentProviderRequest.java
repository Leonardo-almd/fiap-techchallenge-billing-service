package br.com.techchallenge.fiap.billingservice.application.gateway;

import br.com.techchallenge.fiap.billingservice.application.entity.PaymentMethod;
import java.math.BigDecimal;
import lombok.Builder;

/**
 * Request for processing a payment with an external provider (e.g. Mercado Pago or simulator).
 */
@Builder
public record PaymentProviderRequest(
    BigDecimal amount,
    PaymentMethod method,
    String description,
    String externalReference
) {
    public PaymentProviderRequest {
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (method == null) {
            throw new IllegalArgumentException("method must not be null");
        }
        if (externalReference == null || externalReference.isBlank()) {
            throw new IllegalArgumentException("externalReference must not be null or blank");
        }
    }
}

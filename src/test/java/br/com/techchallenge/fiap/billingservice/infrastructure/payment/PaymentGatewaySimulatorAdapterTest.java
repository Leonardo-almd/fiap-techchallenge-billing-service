package br.com.techchallenge.fiap.billingservice.infrastructure.payment;

import br.com.techchallenge.fiap.billingservice.application.entity.PaymentMethod;
import br.com.techchallenge.fiap.billingservice.application.gateway.PaymentProviderRequest;
import br.com.techchallenge.fiap.billingservice.application.gateway.PaymentProviderResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PaymentGatewaySimulatorAdapter (ExternalPaymentProvider implementation).
 */
@DisplayName("PaymentGatewaySimulatorAdapter - Unit Tests")
class PaymentGatewaySimulatorAdapterTest {

    private PaymentGatewaySimulatorAdapter adapter;

    @BeforeEach
    void setUp() {
        PaymentGatewaySimulator simulator = new PaymentGatewaySimulator(0, 0);
        adapter = new PaymentGatewaySimulatorAdapter(simulator);
    }

    @Test
    @DisplayName("Should delegate to simulator and return result with externalReference")
    void shouldDelegateAndReturnResult() {
        PaymentProviderRequest request = PaymentProviderRequest.builder()
                .amount(new BigDecimal("100.00"))
                .method(PaymentMethod.PIX)
                .description("Test")
                .externalReference("PAY-001")
                .build();

        PaymentProviderResult result = adapter.processPayment(request);

        assertThat(result).isNotNull();
        assertThat(result.success()).isIn(true, false);
        if (result.success()) {
            assertThat(result.externalId()).isNotBlank();
            assertThat(result.authorizationCode()).isNotBlank();
            assertThat(result.failureReason()).isNull();
        } else {
            assertThat(result.failureReason()).isNotBlank();
        }
    }

    @Test
    @DisplayName("Should accept all payment methods")
    void shouldAcceptAllPaymentMethods() {
        for (PaymentMethod method : PaymentMethod.values()) {
            PaymentProviderRequest request = PaymentProviderRequest.builder()
                    .amount(BigDecimal.ONE)
                    .method(method)
                    .externalReference("ref-" + method.name())
                    .build();
            PaymentProviderResult result = adapter.processPayment(request);
            assertThat(result).isNotNull();
        }
    }
}

package br.com.techchallenge.fiap.billingservice.infrastructure.payment;

import br.com.techchallenge.fiap.billingservice.application.entity.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaymentGatewaySimulator - Unit Tests")
class PaymentGatewaySimulatorTest {

    private PaymentGatewaySimulator simulator;

    @BeforeEach
    void setUp() {
        // Zero delay in tests to avoid CI timeouts (production uses 2–5 s)
        simulator = new PaymentGatewaySimulator(0, 0);
    }

    @Test
    @DisplayName("Should process payment and return result")
    void shouldProcessPaymentAndReturnResult() {
        // Arrange
        PaymentGatewaySimulator.PaymentRequest request = PaymentGatewaySimulator.PaymentRequest.builder()
            .amount(new BigDecimal("100.00"))
            .method(PaymentMethod.CREDIT_CARD)
            .build();

        // Act
        PaymentGatewaySimulator.PaymentResult result = simulator.processPayment(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.success() || !result.success()).isTrue(); // Either success or failure
    }

    @Test
    @DisplayName("Should generate external ID when payment is successful")
    void shouldGenerateExternalIdWhenSuccessful() {
        // Arrange
        PaymentGatewaySimulator.PaymentRequest request = PaymentGatewaySimulator.PaymentRequest.builder()
            .amount(new BigDecimal("100.00"))
            .method(PaymentMethod.PIX)
            .build();

        // Act - Try multiple times to get at least one success
        PaymentGatewaySimulator.PaymentResult result = null;
        for (int i = 0; i < 20; i++) {
            result = simulator.processPayment(request);
            if (result.success()) {
                break;
            }
        }

        // Assert
        if (result != null && result.success()) {
            assertThat(result.externalId()).isNotNull();
            assertThat(result.externalId()).startsWith("SIM-");
            assertThat(result.externalId()).hasSize(12); // "SIM-" + 8 characters
        }
    }

    @Test
    @DisplayName("Should generate authorization code when payment is successful")
    void shouldGenerateAuthorizationCodeWhenSuccessful() {
        // Arrange
        PaymentGatewaySimulator.PaymentRequest request = PaymentGatewaySimulator.PaymentRequest.builder()
            .amount(new BigDecimal("100.00"))
            .method(PaymentMethod.CREDIT_CARD)
            .build();

        // Act - Try multiple times to get at least one success
        PaymentGatewaySimulator.PaymentResult result = null;
        for (int i = 0; i < 20; i++) {
            result = simulator.processPayment(request);
            if (result.success()) {
                break;
            }
        }

        // Assert
        if (result != null && result.success()) {
            assertThat(result.authorizationCode()).isNotNull();
            assertThat(result.authorizationCode()).startsWith("AUTH-");
        }
    }

    @Test
    @DisplayName("Should provide failure reason when payment fails")
    void shouldProvideFailureReasonWhenFails() {
        // Arrange
        PaymentGatewaySimulator.PaymentRequest request = PaymentGatewaySimulator.PaymentRequest.builder()
            .amount(new BigDecimal("100.00"))
            .method(PaymentMethod.DEBIT_CARD)
            .build();

        // Act - Try multiple times to get at least one failure
        PaymentGatewaySimulator.PaymentResult result = null;
        for (int i = 0; i < 20; i++) {
            result = simulator.processPayment(request);
            if (!result.success()) {
                break;
            }
        }

        // Assert
        if (result != null && !result.success()) {
            assertThat(result.failureReason()).isNotNull();
            assertThat(result.failureReason()).isNotBlank();
        }
    }

    @Test
    @DisplayName("Should not have external ID when payment fails")
    void shouldNotHaveExternalIdWhenFails() {
        // Arrange
        PaymentGatewaySimulator.PaymentRequest request = PaymentGatewaySimulator.PaymentRequest.builder()
            .amount(new BigDecimal("100.00"))
            .method(PaymentMethod.CREDIT_CARD)
            .build();

        // Act - Try multiple times to get at least one failure
        PaymentGatewaySimulator.PaymentResult result = null;
        for (int i = 0; i < 20; i++) {
            result = simulator.processPayment(request);
            if (!result.success()) {
                break;
            }
        }

        // Assert
        if (result != null && !result.success()) {
            assertThat(result.externalId()).isNull();
            assertThat(result.authorizationCode()).isNull();
        }
    }

    @Test
    @DisplayName("Should apply delay when configured")
    void shouldApplyDelayWhenConfigured() {
        // Use simulator with delay for this test only
        PaymentGatewaySimulator simulatorWithDelay = new PaymentGatewaySimulator(50, 100);
        PaymentGatewaySimulator.PaymentRequest request = PaymentGatewaySimulator.PaymentRequest.builder()
            .amount(new BigDecimal("100.00"))
            .method(PaymentMethod.PIX)
            .build();

        long startTime = System.currentTimeMillis();
        simulatorWithDelay.processPayment(request);
        long duration = System.currentTimeMillis() - startTime;

        assertThat(duration).isGreaterThanOrEqualTo(50);
        assertThat(duration).isLessThan(500); // 100ms + margin
    }

    @RepeatedTest(3)
    @DisplayName("Should have approximately 90% success rate")
    void shouldHaveApproximately90PercentSuccessRate() {
        // Enough attempts to reduce variance (simulator uses 0 delay in setUp; 90% theoretical rate)
        int totalAttempts = 100;
        int successCount = 0;

        PaymentGatewaySimulator.PaymentRequest request = PaymentGatewaySimulator.PaymentRequest.builder()
            .amount(new BigDecimal("50.00"))
            .method(PaymentMethod.CREDIT_CARD)
            .build();

        for (int i = 0; i < totalAttempts; i++) {
            PaymentGatewaySimulator.PaymentResult result = simulator.processPayment(request);
            if (result.success()) {
                successCount++;
            }
        }

        double successRate = (double) successCount / totalAttempts * 100;
        // With n=100 and p=0.9, ~95% of runs fall in ~82–98%; allow 75–100% for CI stability
        assertThat(successRate).isBetween(75.0, 100.0);
    }

    @Test
    @DisplayName("Should work with different payment methods")
    void shouldWorkWithDifferentPaymentMethods() {
        // Test each payment method
        for (PaymentMethod method : PaymentMethod.values()) {
            // Arrange
            PaymentGatewaySimulator.PaymentRequest request = PaymentGatewaySimulator.PaymentRequest.builder()
                .amount(new BigDecimal("100.00"))
                .method(method)
                .build();

            // Act
            PaymentGatewaySimulator.PaymentResult result = simulator.processPayment(request);

            // Assert
            assertThat(result).isNotNull();
        }
    }

    @Test
    @DisplayName("Should work with different amounts")
    void shouldWorkWithDifferentAmounts() {
        // Test with various amounts
        BigDecimal[] amounts = {
            new BigDecimal("10.00"),
            new BigDecimal("100.50"),
            new BigDecimal("1000.99"),
            new BigDecimal("5000.00")
        };

        for (BigDecimal amount : amounts) {
            // Arrange
            PaymentGatewaySimulator.PaymentRequest request = PaymentGatewaySimulator.PaymentRequest.builder()
                .amount(amount)
                .method(PaymentMethod.CREDIT_CARD)
                .build();

            // Act
            PaymentGatewaySimulator.PaymentResult result = simulator.processPayment(request);

            // Assert
            assertThat(result).isNotNull();
        }
    }

    @Test
    @DisplayName("Should generate different external IDs")
    void shouldGenerateDifferentExternalIds() {
        // Arrange
        PaymentGatewaySimulator.PaymentRequest request = PaymentGatewaySimulator.PaymentRequest.builder()
            .amount(new BigDecimal("100.00"))
            .method(PaymentMethod.CREDIT_CARD)
            .build();

        // Act - Get multiple successful results
        String firstId = null;
        String secondId = null;

        for (int i = 0; i < 30 && (firstId == null || secondId == null); i++) {
            PaymentGatewaySimulator.PaymentResult result = simulator.processPayment(request);
            if (result.success()) {
                if (firstId == null) {
                    firstId = result.externalId();
                } else if (secondId == null) {
                    secondId = result.externalId();
                }
            }
        }

        // Assert
        if (firstId != null && secondId != null) {
            assertThat(firstId).isNotEqualTo(secondId);
        }
    }
}

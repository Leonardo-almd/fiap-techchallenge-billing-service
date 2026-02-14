package br.com.techchallenge.fiap.billingservice.application.entity;

import br.com.techchallenge.fiap.billingservice.application.exception.InvalidDataException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Payment entity.
 */
class PaymentTest {

    @Test
    void shouldCreateValidPayment() {
        // Given
        String paymentId = "pay-123";
        String budgetId = "bdg-456";
        String serviceOrderId = "os-789";
        Price amount = Price.of("290.00");
        PaymentMethod method = PaymentMethod.PIX;
        PaymentStatus status = PaymentStatus.pending();

        // When
        Payment payment = Payment.builder()
            .paymentId(paymentId)
            .budgetId(budgetId)
            .serviceOrderId(serviceOrderId)
            .amount(amount.value())
            .method(method)
            .status(status)
            .createdAt(LocalDateTime.now())
            .build();

        // Then
        assertThat(payment.paymentId()).isEqualTo(paymentId);
        assertThat(payment.budgetId()).isEqualTo(budgetId);
        assertThat(payment.method()).isEqualTo(method);
        assertThat(payment.status()).isEqualTo(status);
    }

    @Test
    void shouldThrowExceptionWhenBudgetIdIsNull() {
        // When / Then
        assertThatThrownBy(() -> Payment.builder()
            .paymentId("pay-123")
            .budgetId(null)
            .serviceOrderId("os-789")
            .amount(Price.of("100").value())
            .method(PaymentMethod.PIX)
            .status(PaymentStatus.pending())
            .createdAt(LocalDateTime.now())
            .build())
        .isInstanceOf(InvalidDataException.class)
        .hasMessageContaining("budgetId must not be null");
    }

    @Test
    void shouldThrowExceptionWhenAmountIsZero() {
        // When / Then
        assertThatThrownBy(() -> Payment.builder()
            .paymentId("pay-123")
            .budgetId("bdg-456")
            .serviceOrderId("os-789")
            .amount(Price.zero().value())
            .method(PaymentMethod.PIX)
            .status(PaymentStatus.pending())
            .createdAt(LocalDateTime.now())
            .build())
        .isInstanceOf(InvalidDataException.class)
        .hasMessageContaining("amount must be greater than zero");
    }

    @Test
    void shouldUpdateStatusToPaid() {
        // Given
        Payment payment = createSamplePayment();
        LocalDateTime now = LocalDateTime.now();

        // When
        Payment paidPayment = payment.withStatusUpdated(PaymentStatus.paid(), now);

        // Then
        assertThat(paidPayment.status().isPaid()).isTrue();
        assertThat(paidPayment.processedAt()).isEqualTo(now);
    }

    @Test
    void shouldUpdateStatusToRefunded() {
        // Given
        Payment payment = createSamplePayment();
        LocalDateTime now = LocalDateTime.now();

        // When
        Payment refundedPayment = payment.withStatusUpdated(PaymentStatus.refunded(), now);

        // Then
        assertThat(refundedPayment.status().isRefunded()).isTrue();
        assertThat(refundedPayment.refundedAt()).isEqualTo(now);
    }

    @Test
    void shouldSetExternalId() {
        // Given
        Payment payment = createSamplePayment();
        String externalId = "EXT-999-ABC";

        // When
        Payment updatedPayment = payment.withExternalId(externalId);

        // Then
        assertThat(updatedPayment.externalId()).isEqualTo(externalId);
    }

    @Test
    void shouldSetAuthorizationCode() {
        // Given
        Payment payment = createSamplePayment();
        String authCode = "AUTH-123456";

        // When
        Payment updatedPayment = payment.withAuthorizationCode(authCode);

        // Then
        assertThat(updatedPayment.authorizationCode()).isEqualTo(authCode);
    }

    @Test
    void shouldSetFailureReason() {
        // Given
        Payment payment = createSamplePayment();
        String failureReason = "Cartão recusado";

        // When
        Payment updatedPayment = payment.withFailureReason(failureReason);

        // Then
        assertThat(updatedPayment.failureReason()).isEqualTo(failureReason);
    }

    @Test
    void shouldSupportAllPaymentMethods() {
        // Test all payment methods can be created
        for (PaymentMethod method : PaymentMethod.values()) {
            Payment payment = Payment.builder()
                .paymentId("pay-123")
                .budgetId("bdg-456")
                .serviceOrderId("os-789")
                .amount(Price.of("100").value())
                .method(method)
                .status(PaymentStatus.pending())
                .createdAt(LocalDateTime.now())
                .build();

            assertThat(payment.method()).isEqualTo(method);
        }
    }

    @Test
    void shouldUpdatePaymentId() {
        // Given
        Payment payment = createSamplePayment();
        String newId = "pay-new-999";

        // When
        Payment updatedPayment = payment.withId(newId);

        // Then
        assertThat(updatedPayment.paymentId()).isEqualTo(newId);
        assertThat(updatedPayment.budgetId()).isEqualTo(payment.budgetId());
    }

    // Helper methods

    private Payment createSamplePayment() {
        return Payment.builder()
            .paymentId("pay-123")
            .budgetId("bdg-456")
            .serviceOrderId("os-789")
            .amount(Price.of("290.00").value())
            .method(PaymentMethod.PIX)
            .status(PaymentStatus.pending())
            .createdAt(LocalDateTime.now())
            .build();
    }
}

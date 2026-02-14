package br.com.techchallenge.fiap.billingservice.application.usecase.payment;

import br.com.techchallenge.fiap.billingservice.application.entity.Payment;
import br.com.techchallenge.fiap.billingservice.application.entity.PaymentMethod;
import br.com.techchallenge.fiap.billingservice.application.entity.PaymentStatus;
import br.com.techchallenge.fiap.billingservice.application.exception.InvalidDataException;
import br.com.techchallenge.fiap.billingservice.application.exception.NotFoundException;
import br.com.techchallenge.fiap.billingservice.application.gateway.PaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefundPaymentUseCase - Unit Tests")
class RefundPaymentUseCaseTest {

    @Mock
    private PaymentGateway paymentGateway;

    private RefundPaymentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RefundPaymentUseCase(paymentGateway);
    }

    @Test
    @DisplayName("Should refund paid payment")
    void shouldRefundPaidPayment() {
        // Arrange
        String paymentId = "PAY-001";
        Payment paidPayment = createPaidPayment(paymentId);
        when(paymentGateway.findById(paymentId)).thenReturn(Optional.of(paidPayment));
        when(paymentGateway.update(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Payment result = useCase.execute(paymentId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.status().isRefunded()).isTrue();
        verify(paymentGateway).findById(paymentId);
        verify(paymentGateway).update(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when payment not found")
    void shouldThrowNotFoundExceptionWhenPaymentNotFound() {
        // Arrange
        String paymentId = "NON-EXISTENT";
        when(paymentGateway.findById(paymentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(paymentId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Payment not found");
        
        verify(paymentGateway).findById(paymentId);
        verify(paymentGateway, never()).update(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw InvalidDataException when payment is not paid")
    void shouldThrowExceptionWhenPaymentIsNotPaid() {
        // Arrange
        String paymentId = "PAY-001";
        Payment processingPayment = createProcessingPayment(paymentId);
        when(paymentGateway.findById(paymentId)).thenReturn(Optional.of(processingPayment));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(paymentId))
            .isInstanceOf(InvalidDataException.class)
            .hasMessageContaining("Only paid payments can be refunded");
        
        verify(paymentGateway).findById(paymentId);
        verify(paymentGateway, never()).update(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw InvalidDataException when payment already failed")
    void shouldThrowExceptionWhenPaymentAlreadyFailed() {
        // Arrange
        String paymentId = "PAY-001";
        Payment failedPayment = createFailedPayment(paymentId);
        when(paymentGateway.findById(paymentId)).thenReturn(Optional.of(failedPayment));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(paymentId))
            .isInstanceOf(InvalidDataException.class)
            .hasMessageContaining("Only paid payments can be refunded");
    }

    @Test
    @DisplayName("Should set refunded timestamp")
    void shouldSetRefundedTimestamp() {
        // Arrange
        String paymentId = "PAY-001";
        Payment paidPayment = createPaidPayment(paymentId);
        when(paymentGateway.findById(paymentId)).thenReturn(Optional.of(paidPayment));
        when(paymentGateway.update(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Payment result = useCase.execute(paymentId);

        // Assert
        assertThat(result.refundedAt()).isNotNull();
        assertThat(result.updatedAt()).isAfter(paidPayment.updatedAt());
    }

    private Payment createPaidPayment(String paymentId) {
        LocalDateTime now = LocalDateTime.now();
        return Payment.builder()
            .paymentId(paymentId)
            .budgetId("BUDGET-001")
            .serviceOrderId("ORDER-001")
            .amount(new BigDecimal("100.00"))
            .method(PaymentMethod.CREDIT_CARD)
            .status(PaymentStatus.paid())
            .externalId("EXT-001")
            .authorizationCode("AUTH-001")
            .createdAt(now.minusHours(1))
            .updatedAt(now)
            .processedAt(now)
            .build();
    }

    private Payment createProcessingPayment(String paymentId) {
        LocalDateTime now = LocalDateTime.now();
        return Payment.builder()
            .paymentId(paymentId)
            .budgetId("BUDGET-001")
            .serviceOrderId("ORDER-001")
            .amount(new BigDecimal("100.00"))
            .method(PaymentMethod.PIX)
            .status(PaymentStatus.processing())
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    private Payment createFailedPayment(String paymentId) {
        LocalDateTime now = LocalDateTime.now();
        return Payment.builder()
            .paymentId(paymentId)
            .budgetId("BUDGET-001")
            .serviceOrderId("ORDER-001")
            .amount(new BigDecimal("100.00"))
            .method(PaymentMethod.DEBIT_CARD)
            .status(PaymentStatus.failed())
            .failureReason("Saldo insuficiente")
            .createdAt(now.minusMinutes(5))
            .updatedAt(now)
            .build();
    }
}

package br.com.techchallenge.fiap.billingservice.application.usecase.payment;

import br.com.techchallenge.fiap.billingservice.application.entity.*;
import br.com.techchallenge.fiap.billingservice.application.exception.InvalidDataException;
import br.com.techchallenge.fiap.billingservice.application.exception.NotFoundException;
import br.com.techchallenge.fiap.billingservice.application.gateway.BudgetGateway;
import br.com.techchallenge.fiap.billingservice.application.gateway.PaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessPaymentUseCase - Unit Tests")
class ProcessPaymentUseCaseTest {

    @Mock
    private BudgetGateway budgetGateway;

    @Mock
    private PaymentGateway paymentGateway;

    private ProcessPaymentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProcessPaymentUseCase(budgetGateway, paymentGateway);
    }

    @Test
    @DisplayName("Should create payment for approved budget")
    void shouldCreatePaymentForApprovedBudget() {
        // Arrange
        String budgetId = "BUDGET-001";
        Budget approvedBudget = createApprovedBudget(budgetId);

        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(approvedBudget));
        when(paymentGateway.findByBudgetId(budgetId)).thenReturn(Optional.empty());
        when(paymentGateway.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Payment result = useCase.execute(budgetId, PaymentMethod.CREDIT_CARD);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.budgetId()).isEqualTo(budgetId);
        assertThat(result.serviceOrderId()).isEqualTo(approvedBudget.serviceOrderId());
        assertThat(result.amount().value()).isEqualByComparingTo(approvedBudget.totalAmount().value());
        assertThat(result.method()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(result.status().isProcessing()).isTrue();

        verify(budgetGateway).findById(budgetId);
        verify(paymentGateway).findByBudgetId(budgetId);
        verify(paymentGateway).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when budget does not exist")
    void shouldThrowNotFoundExceptionWhenBudgetDoesNotExist() {
        // Arrange
        String budgetId = "NON-EXISTENT";
        when(budgetGateway.findById(budgetId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(budgetId, PaymentMethod.CREDIT_CARD))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Budget not found");

        verify(budgetGateway).findById(budgetId);
        verify(paymentGateway, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw InvalidDataException when budget is not approved")
    void shouldThrowExceptionWhenBudgetIsNotApproved() {
        // Arrange
        String budgetId = "BUDGET-001";
        Budget pendingBudget = createPendingBudget(budgetId);

        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(pendingBudget));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(budgetId, PaymentMethod.CREDIT_CARD))
            .isInstanceOf(InvalidDataException.class)
            .hasMessageContaining("must be approved before payment");

        verify(budgetGateway).findById(budgetId);
        verify(paymentGateway, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw InvalidDataException when budget is already paid")
    void shouldThrowExceptionWhenBudgetIsAlreadyPaid() {
        // Arrange
        String budgetId = "BUDGET-001";
        Budget approvedBudget = createApprovedBudget(budgetId);
        Payment paidPayment = createPaidPayment(budgetId);

        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(approvedBudget));
        when(paymentGateway.findByBudgetId(budgetId)).thenReturn(Optional.of(paidPayment));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(budgetId, PaymentMethod.CREDIT_CARD))
            .isInstanceOf(InvalidDataException.class)
            .hasMessageContaining("already paid");

        verify(budgetGateway).findById(budgetId);
        verify(paymentGateway).findByBudgetId(budgetId);
        verify(paymentGateway, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should generate unique payment ID")
    void shouldGenerateUniquePaymentId() {
        // Arrange
        String budgetId = "BUDGET-001";
        Budget approvedBudget = createApprovedBudget(budgetId);

        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(approvedBudget));
        when(paymentGateway.findByBudgetId(budgetId)).thenReturn(Optional.empty());
        when(paymentGateway.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Payment result = useCase.execute(budgetId, PaymentMethod.PIX);

        // Assert
        assertThat(result.paymentId()).isNotNull();
        assertThat(result.paymentId()).isNotBlank();
    }

    @Test
    @DisplayName("Should create payment with PROCESSING status")
    void shouldCreatePaymentWithProcessingStatus() {
        // Arrange
        String budgetId = "BUDGET-001";
        Budget approvedBudget = createApprovedBudget(budgetId);

        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(approvedBudget));
        when(paymentGateway.findByBudgetId(budgetId)).thenReturn(Optional.empty());
        when(paymentGateway.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Payment result = useCase.execute(budgetId, PaymentMethod.DEBIT_CARD);

        // Assert
        assertThat(result.status().isProcessing()).isTrue();
    }

    @Test
    @DisplayName("Should set timestamps correctly")
    void shouldSetTimestampsCorrectly() {
        // Arrange
        String budgetId = "BUDGET-001";
        Budget approvedBudget = createApprovedBudget(budgetId);

        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(approvedBudget));
        when(paymentGateway.findByBudgetId(budgetId)).thenReturn(Optional.empty());
        when(paymentGateway.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Payment result = useCase.execute(budgetId, PaymentMethod.CREDIT_CARD);

        // Assert
        assertThat(result.createdAt()).isNotNull();
        assertThat(result.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should copy budget amount to payment")
    void shouldCopyBudgetAmountToPayment() {
        // Arrange
        String budgetId = "BUDGET-001";
        BigDecimal budgetAmount = new BigDecimal("350.75");
        Budget approvedBudget = createApprovedBudget(budgetId, budgetAmount);

        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(approvedBudget));
        when(paymentGateway.findByBudgetId(budgetId)).thenReturn(Optional.empty());
        when(paymentGateway.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Payment result = useCase.execute(budgetId, PaymentMethod.PIX);

        // Assert
        assertThat(result.amount().value()).isEqualByComparingTo(budgetAmount);
    }

    @Test
    @DisplayName("Should call gateway save with correct payment")
    void shouldCallGatewaySaveWithCorrectPayment() {
        // Arrange
        String budgetId = "BUDGET-001";
        Budget approvedBudget = createApprovedBudget(budgetId);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(approvedBudget));
        when(paymentGateway.findByBudgetId(budgetId)).thenReturn(Optional.empty());
        when(paymentGateway.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        useCase.execute(budgetId, PaymentMethod.CREDIT_CARD);

        // Assert
        verify(paymentGateway).save(paymentCaptor.capture());
        
        Payment capturedPayment = paymentCaptor.getValue();
        assertThat(capturedPayment.budgetId()).isEqualTo(budgetId);
        assertThat(capturedPayment.status().isProcessing()).isTrue();
        assertThat(capturedPayment.method()).isEqualTo(PaymentMethod.CREDIT_CARD);
    }

    @Test
    @DisplayName("Should support all payment methods")
    void shouldSupportAllPaymentMethods() {
        // Arrange
        String budgetId = "BUDGET-001";
        Budget approvedBudget = createApprovedBudget(budgetId);

        when(budgetGateway.findById(budgetId)).thenReturn(Optional.of(approvedBudget));
        when(paymentGateway.findByBudgetId(budgetId)).thenReturn(Optional.empty());
        when(paymentGateway.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert - Test each payment method
        for (PaymentMethod method : PaymentMethod.values()) {
            reset(paymentGateway);
            when(paymentGateway.findByBudgetId(budgetId)).thenReturn(Optional.empty());
            when(paymentGateway.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Payment result = useCase.execute(budgetId, method);
            
            assertThat(result.method()).isEqualTo(method);
        }
    }

    // Helper methods

    private Budget createApprovedBudget(String budgetId) {
        return createApprovedBudget(budgetId, new BigDecimal("250.00"));
    }

    private Budget createApprovedBudget(String budgetId, BigDecimal amount) {
        return Budget.builder()
            .budgetId(budgetId)
            .serviceOrderId("ORDER-001")
            .customerId("CUST-001")
            .vehicleId("VEH-001")
            .items(List.of())
            .totalAmount(amount)
            .status(BudgetStatus.approved())
            .createdAt(LocalDateTime.now().minusHours(1))
            .updatedAt(LocalDateTime.now())
            .build();
    }

    private Budget createPendingBudget(String budgetId) {
        return Budget.builder()
            .budgetId(budgetId)
            .serviceOrderId("ORDER-001")
            .customerId("CUST-001")
            .vehicleId("VEH-001")
            .items(List.of())
            .totalAmount(new BigDecimal("250.00"))
            .status(BudgetStatus.pendingApproval())
            .createdAt(LocalDateTime.now().minusHours(1))
            .updatedAt(LocalDateTime.now().minusHours(1))
            .build();
    }

    private Payment createPaidPayment(String budgetId) {
        LocalDateTime now = LocalDateTime.now();
        return Payment.builder()
            .paymentId("PAY-001")
            .budgetId(budgetId)
            .serviceOrderId("ORDER-001")
            .amount(new BigDecimal("250.00"))
            .method(PaymentMethod.CREDIT_CARD)
            .status(PaymentStatus.paid())
            .externalId("EXT-001")
            .authorizationCode("AUTH-001")
            .createdAt(now.minusMinutes(10))
            .updatedAt(now)
            .processedAt(now)
            .build();
    }
}

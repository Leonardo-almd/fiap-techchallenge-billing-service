package br.com.techchallenge.fiap.billingservice.application.usecase.payment;

import br.com.techchallenge.fiap.billingservice.application.entity.Payment;
import br.com.techchallenge.fiap.billingservice.application.entity.PaymentMethod;
import br.com.techchallenge.fiap.billingservice.application.entity.PaymentStatus;
import br.com.techchallenge.fiap.billingservice.application.entity.Price;
import br.com.techchallenge.fiap.billingservice.application.exception.NotFoundException;
import br.com.techchallenge.fiap.billingservice.application.gateway.PaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindPaymentUseCase - Unit Tests")
class FindPaymentUseCaseTest {

    @Mock
    private PaymentGateway paymentGateway;

    private FindPaymentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindPaymentUseCase(paymentGateway);
    }

    @Test
    @DisplayName("Should find payment by ID")
    void shouldFindPaymentById() {
        String paymentId = "PAY-001";
        Payment payment = createPayment(paymentId, "BUDGET-001");
        when(paymentGateway.findById(paymentId)).thenReturn(Optional.of(payment));

        Payment result = useCase.findById(paymentId);

        assertThat(result).isNotNull();
        assertThat(result.paymentId()).isEqualTo(paymentId);
        verify(paymentGateway).findById(paymentId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when payment not found by ID")
    void shouldThrowNotFoundExceptionWhenPaymentNotFoundById() {
        String paymentId = "NON-EXISTENT";
        when(paymentGateway.findById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.findById(paymentId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Payment not found");

        verify(paymentGateway).findById(paymentId);
    }

    @Test
    @DisplayName("Should find payment by budget ID")
    void shouldFindPaymentByBudgetId() {
        String budgetId = "BUDGET-001";
        Payment payment = createPayment("PAY-001", budgetId);
        when(paymentGateway.findByBudgetId(budgetId)).thenReturn(Optional.of(payment));

        Payment result = useCase.findByBudgetId(budgetId);

        assertThat(result).isNotNull();
        assertThat(result.budgetId()).isEqualTo(budgetId);
        verify(paymentGateway).findByBudgetId(budgetId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when payment not found by budget ID")
    void shouldThrowNotFoundExceptionWhenPaymentNotFoundByBudgetId() {
        String budgetId = "BUDGET-NONE";
        when(paymentGateway.findByBudgetId(budgetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.findByBudgetId(budgetId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Payment not found for budget");

        verify(paymentGateway).findByBudgetId(budgetId);
    }

    @Test
    @DisplayName("Should find payments by service order ID")
    void shouldFindPaymentsByServiceOrderId() {
        String serviceOrderId = "ORDER-001";
        List<Payment> payments = List.of(
            createPayment("PAY-1", "B1"),
            createPayment("PAY-2", "B2")
        );
        when(paymentGateway.findByServiceOrderId(serviceOrderId)).thenReturn(payments);

        List<Payment> result = useCase.findByServiceOrderId(serviceOrderId);

        assertThat(result).hasSize(2);
        verify(paymentGateway).findByServiceOrderId(serviceOrderId);
    }

    @Test
    @DisplayName("Should find all payments with pagination")
    void shouldFindAllPaymentsWithPagination() {
        List<Payment> payments = List.of(createPayment("PAY-1", "B1"), createPayment("PAY-2", "B2"));
        when(paymentGateway.findAll(0, 10)).thenReturn(payments);

        List<Payment> result = useCase.findAll(0, 10);

        assertThat(result).hasSize(2);
        verify(paymentGateway).findAll(0, 10);
    }

    @Test
    @DisplayName("Should throw when page is negative")
    void shouldThrowWhenPageIsNegative() {
        assertThatThrownBy(() -> useCase.findAll(-1, 10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Page number must not be negative");
        verify(paymentGateway, never()).findAll(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should throw when size is zero or negative")
    void shouldThrowWhenSizeIsZeroOrNegative() {
        assertThatThrownBy(() -> useCase.findAll(0, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Page size must be positive");
        assertThatThrownBy(() -> useCase.findAll(0, -1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Page size must be positive");
        verify(paymentGateway, never()).findAll(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should count payments")
    void shouldCountPayments() {
        when(paymentGateway.count()).thenReturn(5L);

        long result = useCase.count();

        assertThat(result).isEqualTo(5L);
        verify(paymentGateway).count();
    }

    private Payment createPayment(String paymentId, String budgetId) {
        return Payment.builder()
            .paymentId(paymentId)
            .budgetId(budgetId)
            .serviceOrderId("ORDER-001")
            .amount(Price.of("100.00").value())
            .method(PaymentMethod.PIX)
            .status(PaymentStatus.pending())
            .createdAt(LocalDateTime.now())
            .build();
    }
}

package br.com.techchallenge.fiap.billingservice.application.controller;

import br.com.techchallenge.fiap.billingservice.application.dto.PageDto;
import br.com.techchallenge.fiap.billingservice.application.dto.PaymentDto;
import br.com.techchallenge.fiap.billingservice.application.dto.PaymentRequestDto;
import br.com.techchallenge.fiap.billingservice.application.entity.Payment;
import br.com.techchallenge.fiap.billingservice.application.entity.PaymentMethod;
import br.com.techchallenge.fiap.billingservice.application.entity.PaymentStatus;
import br.com.techchallenge.fiap.billingservice.application.entity.Price;
import br.com.techchallenge.fiap.billingservice.application.usecase.payment.FindPaymentUseCase;
import br.com.techchallenge.fiap.billingservice.application.usecase.payment.ProcessPaymentUseCase;
import br.com.techchallenge.fiap.billingservice.application.usecase.payment.RefundPaymentUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCleanArchController - Unit Tests")
class PaymentCleanArchControllerTest {

    @Mock
    private ProcessPaymentUseCase processPaymentUseCase;
    @Mock
    private FindPaymentUseCase findPaymentUseCase;
    @Mock
    private RefundPaymentUseCase refundPaymentUseCase;

    private PaymentCleanArchController controller;

    @BeforeEach
    void setUp() {
        controller = new PaymentCleanArchController(
            processPaymentUseCase, findPaymentUseCase, refundPaymentUseCase
        );
    }

    @Test
    @DisplayName("Should process payment and return DTO")
    void shouldProcessPaymentAndReturnDto() {
        PaymentRequestDto request = new PaymentRequestDto("BUDGET-001", PaymentMethod.PIX);
        Payment payment = createPayment("PAY-001", "BUDGET-001");
        when(processPaymentUseCase.execute("BUDGET-001", PaymentMethod.PIX)).thenReturn(payment);

        PaymentDto result = controller.processPayment(request);

        assertThat(result).isNotNull();
        assertThat(result.paymentId()).isEqualTo("PAY-001");
        verify(processPaymentUseCase).execute("BUDGET-001", PaymentMethod.PIX);
    }

    @Test
    @DisplayName("Should find by id and return DTO")
    void shouldFindByIdAndReturnDto() {
        Payment payment = createPayment("PAY-001", "BUDGET-001");
        when(findPaymentUseCase.findById("PAY-001")).thenReturn(payment);

        PaymentDto result = controller.findById("PAY-001");

        assertThat(result).isNotNull();
        assertThat(result.paymentId()).isEqualTo("PAY-001");
        verify(findPaymentUseCase).findById("PAY-001");
    }

    @Test
    @DisplayName("Should find by budget id and return DTO")
    void shouldFindByBudgetIdAndReturnDto() {
        Payment payment = createPayment("PAY-001", "BUDGET-001");
        when(findPaymentUseCase.findByBudgetId("BUDGET-001")).thenReturn(payment);

        PaymentDto result = controller.findByBudgetId("BUDGET-001");

        assertThat(result).isNotNull();
        assertThat(result.budgetId()).isEqualTo("BUDGET-001");
        verify(findPaymentUseCase).findByBudgetId("BUDGET-001");
    }

    @Test
    @DisplayName("Should find by service order id and return list")
    void shouldFindByServiceOrderIdAndReturnList() {
        List<Payment> payments = List.of(createPayment("PAY-1", "B1"), createPayment("PAY-2", "B2"));
        when(findPaymentUseCase.findByServiceOrderId("ORDER-001")).thenReturn(payments);

        List<PaymentDto> result = controller.findByServiceOrderId("ORDER-001");

        assertThat(result).hasSize(2);
        verify(findPaymentUseCase).findByServiceOrderId("ORDER-001");
    }

    @Test
    @DisplayName("Should find all paginated and return PageDto")
    void shouldFindAllPaginatedAndReturnPageDto() {
        List<Payment> payments = List.of(createPayment("PAY-001", "B1"));
        when(findPaymentUseCase.findAll(0, 10)).thenReturn(payments);
        when(findPaymentUseCase.count()).thenReturn(1L);

        PageDto<PaymentDto> result = controller.findAll(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
        verify(findPaymentUseCase).findAll(0, 10);
        verify(findPaymentUseCase).count();
    }

    @Test
    @DisplayName("Should refund and return DTO")
    void shouldRefundAndReturnDto() {
        Payment refunded = createPayment("PAY-001", "BUDGET-001");
        when(refundPaymentUseCase.execute("PAY-001")).thenReturn(refunded);

        PaymentDto result = controller.refund("PAY-001");

        assertThat(result).isNotNull();
        verify(refundPaymentUseCase).execute("PAY-001");
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

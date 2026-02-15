package br.com.techchallenge.fiap.billingservice.application.presenter;

import br.com.techchallenge.fiap.billingservice.application.dto.PaymentDto;
import br.com.techchallenge.fiap.billingservice.application.entity.Payment;
import br.com.techchallenge.fiap.billingservice.application.entity.PaymentMethod;
import br.com.techchallenge.fiap.billingservice.application.entity.PaymentStatus;
import br.com.techchallenge.fiap.billingservice.application.entity.Price;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaymentPresenter - Unit Tests")
class PaymentPresenterTest {

    @Test
    @DisplayName("Should convert Payment to PaymentDto")
    void shouldConvertPaymentToDto() {
        Payment payment = createPayment("PAY-001", "BUDGET-001");

        PaymentDto dto = PaymentPresenter.toDto(payment);

        assertThat(dto).isNotNull();
        assertThat(dto.paymentId()).isEqualTo("PAY-001");
        assertThat(dto.budgetId()).isEqualTo("BUDGET-001");
        assertThat(dto.serviceOrderId()).isEqualTo("ORDER-001");
        assertThat(dto.amount()).isEqualByComparingTo(payment.amount().value());
        assertThat(dto.method()).isEqualTo(payment.method());
        assertThat(dto.status()).isEqualTo(payment.status().name());
    }

    @Test
    @DisplayName("Should return null when payment is null")
    void shouldReturnNullWhenPaymentIsNull() {
        PaymentDto dto = PaymentPresenter.toDto(null);
        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("Should convert list of Payments to list of DTOs")
    void shouldConvertListToDtoList() {
        List<Payment> payments = List.of(
            createPayment("PAY-001", "B1"),
            createPayment("PAY-002", "B2")
        );

        List<PaymentDto> dtos = PaymentPresenter.toDtoList(payments);

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).paymentId()).isEqualTo("PAY-001");
        assertThat(dtos.get(1).paymentId()).isEqualTo("PAY-002");
    }

    @Test
    @DisplayName("Should return empty list when payment list is null")
    void shouldReturnEmptyListWhenListIsNull() {
        List<PaymentDto> dtos = PaymentPresenter.toDtoList(null);
        assertThat(dtos).isEmpty();
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

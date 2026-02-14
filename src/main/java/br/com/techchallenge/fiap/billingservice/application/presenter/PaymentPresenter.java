package br.com.techchallenge.fiap.billingservice.application.presenter;

import br.com.techchallenge.fiap.billingservice.application.dto.PaymentDto;
import br.com.techchallenge.fiap.billingservice.application.entity.Payment;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Presenter for converting Payment entities to DTOs.
 */
public class PaymentPresenter {

    public static PaymentDto toDto(Payment payment) {
        if (payment == null) {
            return null;
        }

        return new PaymentDto(
            payment.paymentId(),
            payment.budgetId(),
            payment.serviceOrderId(),
            payment.amount().value(),
            payment.method(),
            payment.status().name(),
            payment.externalId(),
            payment.authorizationCode(),
            payment.createdAt(),
            payment.updatedAt(),
            payment.processedAt(),
            payment.refundedAt(),
            payment.failureReason()
        );
    }

    public static List<PaymentDto> toDtoList(List<Payment> payments) {
        if (payments == null) {
            return List.of();
        }

        return payments.stream()
            .map(PaymentPresenter::toDto)
            .collect(Collectors.toList());
    }
}

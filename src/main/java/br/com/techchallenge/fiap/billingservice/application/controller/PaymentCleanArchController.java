package br.com.techchallenge.fiap.billingservice.application.controller;

import br.com.techchallenge.fiap.billingservice.application.dto.PageDto;
import br.com.techchallenge.fiap.billingservice.application.dto.PaymentDto;
import br.com.techchallenge.fiap.billingservice.application.dto.PaymentRequestDto;
import br.com.techchallenge.fiap.billingservice.application.entity.Payment;
import br.com.techchallenge.fiap.billingservice.application.presenter.PaymentPresenter;
import br.com.techchallenge.fiap.billingservice.application.usecase.payment.FindPaymentUseCase;
import br.com.techchallenge.fiap.billingservice.application.usecase.payment.ProcessPaymentUseCase;
import br.com.techchallenge.fiap.billingservice.application.usecase.payment.RefundPaymentUseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Clean Architecture controller for Payment operations.
 * This is the application layer controller (port) that coordinates use cases.
 */
@RequiredArgsConstructor
public class PaymentCleanArchController {

    private final ProcessPaymentUseCase processPaymentUseCase;
    private final FindPaymentUseCase findPaymentUseCase;
    private final RefundPaymentUseCase refundPaymentUseCase;

    /**
     * Process a payment.
     */
    public PaymentDto processPayment(PaymentRequestDto request) {
        Payment payment = processPaymentUseCase.execute(
            request.budgetId(),
            request.method()
        );
        return PaymentPresenter.toDto(payment);
    }

    /**
     * Find payment by ID.
     */
    public PaymentDto findById(String paymentId) {
        Payment payment = findPaymentUseCase.findById(paymentId);
        return PaymentPresenter.toDto(payment);
    }

    /**
     * Find payment by budget ID.
     */
    public PaymentDto findByBudgetId(String budgetId) {
        Payment payment = findPaymentUseCase.findByBudgetId(budgetId);
        return PaymentPresenter.toDto(payment);
    }

    /**
     * Find payments by service order ID.
     */
    public List<PaymentDto> findByServiceOrderId(String serviceOrderId) {
        List<Payment> payments = findPaymentUseCase.findByServiceOrderId(serviceOrderId);
        return PaymentPresenter.toDtoList(payments);
    }

    /**
     * Find all payments (paginated).
     */
    public PageDto<PaymentDto> findAll(int page, int size) {
        List<Payment> payments = findPaymentUseCase.findAll(page, size);
        long total = findPaymentUseCase.count();
        
        List<PaymentDto> paymentDtos = PaymentPresenter.toDtoList(payments);
        
        return PageDto.of(paymentDtos, page, size, total);
    }

    /**
     * Refund a payment (compensation).
     */
    public PaymentDto refund(String paymentId) {
        Payment refundedPayment = refundPaymentUseCase.execute(paymentId);
        return PaymentPresenter.toDto(refundedPayment);
    }
}

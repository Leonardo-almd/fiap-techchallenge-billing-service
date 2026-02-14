package br.com.techchallenge.fiap.billingservice.application.usecase.payment;

import br.com.techchallenge.fiap.billingservice.application.entity.Payment;
import br.com.techchallenge.fiap.billingservice.application.exception.NotFoundException;
import br.com.techchallenge.fiap.billingservice.application.gateway.PaymentGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Use case for finding payments.
 */
@RequiredArgsConstructor
public class FindPaymentUseCase {

    private final PaymentGateway paymentGateway;

    /**
     * Find a payment by ID.
     */
    public Payment findById(String paymentId) {
        return paymentGateway.findById(paymentId)
            .orElseThrow(() -> new NotFoundException("Payment not found with id: " + paymentId));
    }

    /**
     * Find payment by budget ID.
     */
    public Payment findByBudgetId(String budgetId) {
        return paymentGateway.findByBudgetId(budgetId)
            .orElseThrow(() -> new NotFoundException("Payment not found for budget: " + budgetId));
    }

    /**
     * Find all payments for a service order.
     */
    public List<Payment> findByServiceOrderId(String serviceOrderId) {
        return paymentGateway.findByServiceOrderId(serviceOrderId);
    }

    /**
     * Find all payments (paginated).
     */
    public List<Payment> findAll(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must not be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        
        return paymentGateway.findAll(page, size);
    }

    /**
     * Count total payments.
     */
    public long count() {
        return paymentGateway.count();
    }
}

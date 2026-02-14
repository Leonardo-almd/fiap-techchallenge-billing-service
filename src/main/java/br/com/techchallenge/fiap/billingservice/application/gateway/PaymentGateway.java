package br.com.techchallenge.fiap.billingservice.application.gateway;

import br.com.techchallenge.fiap.billingservice.application.entity.Payment;
import java.util.List;
import java.util.Optional;

/**
 * Gateway (port) for Payment persistence operations.
 * This is an interface in the application layer that will be implemented
 * by the infrastructure layer (e.g., DynamoDB repository).
 */
public interface PaymentGateway {

    /**
     * Save a new payment or update an existing one.
     */
    Payment save(Payment payment);

    /**
     * Update an existing payment.
     */
    Payment update(Payment payment);

    /**
     * Find a payment by ID.
     */
    Optional<Payment> findById(String paymentId);

    /**
     * Find a payment by budget ID.
     */
    Optional<Payment> findByBudgetId(String budgetId);

    /**
     * Find all payments for a service order.
     */
    List<Payment> findByServiceOrderId(String serviceOrderId);

    /**
     * Find all payments (paginated).
     */
    List<Payment> findAll(int page, int size);

    /**
     * Count total payments.
     */
    long count();

    /**
     * Delete a payment by ID.
     */
    void deleteById(String paymentId);
}

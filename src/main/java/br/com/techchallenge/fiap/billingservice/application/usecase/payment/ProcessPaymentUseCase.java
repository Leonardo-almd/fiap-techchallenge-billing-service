package br.com.techchallenge.fiap.billingservice.application.usecase.payment;

import br.com.techchallenge.fiap.billingservice.application.entity.Budget;
import br.com.techchallenge.fiap.billingservice.application.entity.Payment;
import br.com.techchallenge.fiap.billingservice.application.entity.PaymentMethod;
import br.com.techchallenge.fiap.billingservice.application.entity.PaymentStatus;
import br.com.techchallenge.fiap.billingservice.application.exception.InvalidDataException;
import br.com.techchallenge.fiap.billingservice.application.exception.NotFoundException;
import br.com.techchallenge.fiap.billingservice.application.gateway.BudgetGateway;
import br.com.techchallenge.fiap.billingservice.application.gateway.PaymentGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case for processing a payment.
 */
@RequiredArgsConstructor
public class ProcessPaymentUseCase {

    private final BudgetGateway budgetGateway;
    private final PaymentGateway paymentGateway;

    /**
     * Execute the use case to process a payment for an approved budget.
     * 
     * @param budgetId Budget ID
     * @param method Payment method
     * @return Created payment (PROCESSING status - will be updated by PaymentGatewaySimulator)
     */
    public Payment execute(String budgetId, PaymentMethod method) {
        // Find approved budget
        Budget budget = budgetGateway.findById(budgetId)
            .orElseThrow(() -> new NotFoundException("Budget not found with id: " + budgetId));

        // Validate budget is approved
        if (!budget.status().isApproved()) {
            throw new InvalidDataException(
                "Budget must be approved before payment. Current status: " + budget.status().name()
            );
        }

        // Check if payment already exists
        Optional<Payment> existingPayment = paymentGateway.findByBudgetId(budgetId);
        if (existingPayment.isPresent() && existingPayment.get().status().isPaid()) {
            throw new InvalidDataException("Budget already paid");
        }

        // Create payment with PROCESSING status
        LocalDateTime now = LocalDateTime.now();
        Payment payment = Payment.builder()
            .paymentId(UUID.randomUUID().toString())
            .budgetId(budgetId)
            .serviceOrderId(budget.serviceOrderId())
            .amount(budget.totalAmount().value())
            .method(method)
            .status(PaymentStatus.processing())
            .createdAt(now)
            .updatedAt(now)
            .build();

        // Save payment
        return paymentGateway.save(payment);
    }
}

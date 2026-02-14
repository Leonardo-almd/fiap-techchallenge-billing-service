package br.com.techchallenge.fiap.billingservice.application.usecase.payment;

import br.com.techchallenge.fiap.billingservice.application.entity.Payment;
import br.com.techchallenge.fiap.billingservice.application.entity.PaymentStatus;
import br.com.techchallenge.fiap.billingservice.application.exception.InvalidDataException;
import br.com.techchallenge.fiap.billingservice.application.exception.NotFoundException;
import br.com.techchallenge.fiap.billingservice.application.gateway.PaymentGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * Use case for refunding a payment (compensation/rollback).
 */
@RequiredArgsConstructor
public class RefundPaymentUseCase {

    private final PaymentGateway paymentGateway;

    /**
     * Execute the use case to refund a payment.
     * This is part of the Saga compensation pattern.
     * 
     * @param paymentId Payment ID to refund
     * @return Refunded payment
     * @throws NotFoundException if payment not found
     * @throws InvalidDataException if payment is not paid
     */
    public Payment execute(String paymentId) {
        // Find payment
        Payment payment = paymentGateway.findById(paymentId)
            .orElseThrow(() -> new NotFoundException("Payment not found with id: " + paymentId));

        // Validate status
        if (!payment.status().isPaid()) {
            throw new InvalidDataException(
                "Only paid payments can be refunded. Current status: " + payment.status().name()
            );
        }

        // Update status to REFUNDED
        Payment refundedPayment = payment.withStatusUpdated(
            PaymentStatus.refunded(),
            LocalDateTime.now()
        );

        // Save and return
        return paymentGateway.update(refundedPayment);
    }
}

package br.com.techchallenge.fiap.billingservice.infrastructure.orchestration;

import br.com.techchallenge.fiap.billingservice.application.entity.Budget;
import br.com.techchallenge.fiap.billingservice.application.entity.Payment;
import br.com.techchallenge.fiap.billingservice.application.entity.PaymentStatus;
import br.com.techchallenge.fiap.billingservice.application.gateway.BudgetGateway;
import br.com.techchallenge.fiap.billingservice.application.gateway.PaymentGateway;
import br.com.techchallenge.fiap.billingservice.infrastructure.messaging.event.*;
import br.com.techchallenge.fiap.billingservice.infrastructure.messaging.publisher.SqsEventPublisher;
import br.com.techchallenge.fiap.billingservice.infrastructure.payment.PaymentGatewaySimulator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Orchestrator for processing pending payments.
 * This service polls for PROCESSING payments and simulates payment with the gateway.
 * Part of the Saga choreography pattern.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessingOrchestrator {

    private final PaymentGateway paymentGateway;
    private final BudgetGateway budgetGateway;
    private final PaymentGatewaySimulator paymentGatewaySimulator;
    private final SqsEventPublisher eventPublisher;

    /**
     * Process pending payments.
     * Runs every 10 seconds.
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    public void processPendingPayments() {
        try {
            log.info("🔍 Checking for pending payments...");

            // Find all PROCESSING payments (simple implementation - in production use better query)
            List<Payment> allPayments = paymentGateway.findAll(0, 100);
            
            List<Payment> processingPayments = allPayments.stream()
                .filter(p -> p.status().isProcessing())
                .toList();

            if (processingPayments.isEmpty()) {
                log.debug("No pending payments to process");
                return;
            }

            log.info("📋 Found {} payments to process", processingPayments.size());

            for (Payment payment : processingPayments) {
                processPayment(payment);
            }

        } catch (Exception e) {
            log.error("❌ Error processing pending payments", e);
        }
    }

    /**
     * Process a single payment using the payment gateway simulator.
     */
    private void processPayment(Payment payment) {
        try {
            log.info("💳 Processing payment: {}", payment.paymentId());

            // Call payment gateway simulator
            PaymentGatewaySimulator.PaymentRequest request = PaymentGatewaySimulator.PaymentRequest.builder()
                .amount(payment.amount().value())
                .method(payment.method())
                .build();

            PaymentGatewaySimulator.PaymentResult result = paymentGatewaySimulator.processPayment(request);

            if (result.success()) {
                handlePaymentSuccess(payment, result);
            } else {
                handlePaymentFailure(payment, result);
            }

        } catch (Exception e) {
            log.error("❌ Error processing payment: {}", payment.paymentId(), e);
        }
    }

    /**
     * Handle successful payment.
     */
    private void handlePaymentSuccess(Payment payment, PaymentGatewaySimulator.PaymentResult result) {
        log.info("✅ Payment successful: {}", payment.paymentId());

        LocalDateTime now = LocalDateTime.now();

        // Update payment to PAID
        Payment paidPayment = payment.withStatusUpdated(PaymentStatus.paid(), now)
            .withExternalId(result.externalId())
            .withAuthorizationCode(result.authorizationCode());

        paymentGateway.update(paidPayment);

        // Publish PaymentProcessedEvent
        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .paymentId(paidPayment.paymentId())
            .budgetId(paidPayment.budgetId())
            .serviceOrderId(paidPayment.serviceOrderId())
            .amount(paidPayment.amount().value())
            .method(paidPayment.method().name())
            .externalId(paidPayment.externalId())
            .authorizationCode(paidPayment.authorizationCode())
            .paidAt(now)
            .timestamp(now)
            .build();

        eventPublisher.publishEvent(event);

        log.info("📤 PaymentProcessedEvent published for payment: {}", paidPayment.paymentId());
    }

    /**
     * Handle payment failure.
     */
    private void handlePaymentFailure(Payment payment, PaymentGatewaySimulator.PaymentResult result) {
        log.warn("❌ Payment failed: {} - Reason: {}", payment.paymentId(), result.failureReason());

        LocalDateTime now = LocalDateTime.now();

        // Update payment to FAILED
        Payment failedPayment = payment.withStatusUpdated(PaymentStatus.failed(), now)
            .withFailureReason(result.failureReason());

        paymentGateway.update(failedPayment);

        // Publish PaymentFailedEvent
        PaymentFailedEvent event = PaymentFailedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .paymentId(failedPayment.paymentId())
            .budgetId(failedPayment.budgetId())
            .serviceOrderId(failedPayment.serviceOrderId())
            .failureReason(result.failureReason())
            .failedAt(now)
            .timestamp(now)
            .build();

        eventPublisher.publishEvent(event);

        log.info("📤 PaymentFailedEvent published for payment: {}", failedPayment.paymentId());
    }
}

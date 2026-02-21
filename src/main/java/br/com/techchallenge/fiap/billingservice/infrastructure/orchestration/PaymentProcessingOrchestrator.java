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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrator for processing pending payments.
 * This service polls for PROCESSING payments and simulates payment with the
 * gateway.
 * Part of the Saga choreography pattern.
 * Publishes to:
 * - billing-events.fifo (FIFO, for Execution Service)
 * - payment-failed-queue (Standard, for OS Service → CANCELLED compensation)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessingOrchestrator {

    private final PaymentGateway paymentGateway;
    private final BudgetGateway budgetGateway;
    private final PaymentGatewaySimulator paymentGatewaySimulator;
    private final SqsEventPublisher eventPublisher;

    @Value("${aws.sqs.queues.payment-failed-url}")
    private String paymentFailedQueueUrl;

    /**
     * Process pending payments.
     * Runs every 10 seconds.
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    public void processPendingPayments() {
        try {
            log.info("Checking for pending payments...");

            List<Payment> allPayments = paymentGateway.findAll(0, 100);

            List<Payment> processingPayments = allPayments.stream()
                    .filter(p -> p.status().isProcessing())
                    .toList();

            if (processingPayments.isEmpty()) {
                log.debug("No pending payments to process");
                return;
            }

            log.info("Found {} payments to process", processingPayments.size());

            for (Payment payment : processingPayments) {
                processPayment(payment);
            }

        } catch (Exception e) {
            log.error("Error processing pending payments", e);
        }
    }

    private void processPayment(Payment payment) {
        try {
            log.info("Processing payment: {}", payment.paymentId());

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
            log.error("Error processing payment: {}", payment.paymentId(), e);
        }
    }

    private void handlePaymentSuccess(Payment payment, PaymentGatewaySimulator.PaymentResult result) {
        log.info("Payment successful: {}", payment.paymentId());

        LocalDateTime now = LocalDateTime.now();

        Payment paidPayment = payment.withStatusUpdated(PaymentStatus.paid(), now)
                .withExternalId(result.externalId())
                .withAuthorizationCode(result.authorizationCode());

        paymentGateway.update(paidPayment);

        // Publish PaymentProcessedEvent to billing-events.fifo (Execution Service
        // creates task)
        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                .eventType("PaymentProcessed")
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

        log.info("PaymentProcessedEvent published for payment: {}", paidPayment.paymentId());
    }

    private void handlePaymentFailure(Payment payment, PaymentGatewaySimulator.PaymentResult result) {
        log.warn("Payment failed: {} - Reason: {}", payment.paymentId(), result.failureReason());

        LocalDateTime now = LocalDateTime.now();

        Payment failedPayment = payment.withStatusUpdated(PaymentStatus.failed(), now)
                .withFailureReason(result.failureReason());

        paymentGateway.update(failedPayment);

        // Publish PaymentFailedEvent to billing-events.fifo (Execution Service cancels
        // task)
        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .eventType("PaymentFailed")
                .eventId(UUID.randomUUID().toString())
                .paymentId(failedPayment.paymentId())
                .budgetId(failedPayment.budgetId())
                .serviceOrderId(failedPayment.serviceOrderId())
                .failureReason(result.failureReason())
                .failedAt(now)
                .timestamp(now)
                .build();

        eventPublisher.publishEvent(event);

        // Also publish to payment-failed-queue (OS Service: Saga compensation →
        // CANCELLED)
        try {
            Map<String, Object> osPayload = new HashMap<>();
            osPayload.put("orderId", Long.parseLong(failedPayment.serviceOrderId()));
            osPayload.put("reason", result.failureReason());
            osPayload.put("paymentId", failedPayment.paymentId());
            osPayload.put("timestamp", now.toString());

            eventPublisher.publishToStandardQueue(paymentFailedQueueUrl, osPayload);
            log.info("PaymentFailed published to payment-failed-queue for OS: {}", failedPayment.serviceOrderId());
        } catch (Exception e) {
            log.error("Failed to publish to payment-failed-queue for OS: {}", failedPayment.serviceOrderId(), e);
        }

        log.info("PaymentFailedEvent published for payment: {}", failedPayment.paymentId());
    }
}

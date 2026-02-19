package br.com.techchallenge.fiap.billingservice.infrastructure.orchestration;

import br.com.techchallenge.fiap.billingservice.application.entity.Payment;
import br.com.techchallenge.fiap.billingservice.infrastructure.messaging.event.PaymentRefundedEvent;
import br.com.techchallenge.fiap.billingservice.infrastructure.messaging.publisher.SqsEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Orchestrator for publishing payment-related events.
 * Handles PaymentRefundedEvent publishing as part of the Saga compensation pattern.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventOrchestrator {

    private final SqsEventPublisher eventPublisher;

    /**
     * Publish PaymentRefundedEvent to billing-events FIFO queue.
     * This triggers Execution Service to cancel the execution task (Saga compensation).
     */
    public void publishPaymentRefunded(Payment payment) {
        log.info("Publishing PaymentRefundedEvent for payment: {}", payment.paymentId());

        PaymentRefundedEvent event = PaymentRefundedEvent.builder()
            .eventType("PaymentRefunded")
            .eventId(UUID.randomUUID().toString())
            .paymentId(payment.paymentId())
            .budgetId(payment.budgetId())
            .serviceOrderId(payment.serviceOrderId())
            .refundedAt(payment.updatedAt())
            .timestamp(LocalDateTime.now())
            .build();

        eventPublisher.publishEvent(event);

        log.info("PaymentRefundedEvent published for payment: {}", payment.paymentId());
    }
}

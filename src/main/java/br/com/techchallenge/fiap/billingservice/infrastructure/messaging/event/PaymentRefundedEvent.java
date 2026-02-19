package br.com.techchallenge.fiap.billingservice.infrastructure.messaging.event;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Event published when a payment is refunded (compensation).
 * Part of the Saga choreography pattern.
 */
@Builder
public record PaymentRefundedEvent(
    String eventType,
    String eventId,
    String paymentId,
    String budgetId,
    String serviceOrderId,
    LocalDateTime refundedAt,
    LocalDateTime timestamp
) {}

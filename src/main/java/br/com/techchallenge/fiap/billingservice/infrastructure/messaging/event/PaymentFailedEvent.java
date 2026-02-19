package br.com.techchallenge.fiap.billingservice.infrastructure.messaging.event;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Event published when a payment fails.
 * Part of the Saga choreography pattern (compensation trigger).
 */
@Builder
public record PaymentFailedEvent(
    String eventType,
    String eventId,
    String paymentId,
    String budgetId,
    String serviceOrderId,
    String failureReason,
    LocalDateTime failedAt,
    LocalDateTime timestamp
) {}

package br.com.techchallenge.fiap.billingservice.infrastructure.messaging.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when a payment is successfully processed.
 * Part of the Saga choreography pattern.
 */
@Builder
public record PaymentProcessedEvent(
    String eventId,
    String paymentId,
    String budgetId,
    String serviceOrderId,
    BigDecimal amount,
    String method,
    String externalId,
    String authorizationCode,
    LocalDateTime paidAt,
    LocalDateTime timestamp
) {}

package br.com.techchallenge.fiap.billingservice.infrastructure.messaging.event;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Event published when a budget is rejected.
 * Part of the Saga choreography pattern.
 */
@Builder
public record BudgetRejectedEvent(
    String eventId,
    String budgetId,
    String serviceOrderId,
    String customerId,
    LocalDateTime rejectedAt,
    LocalDateTime timestamp
) {}

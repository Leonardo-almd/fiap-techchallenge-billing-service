package br.com.techchallenge.fiap.billingservice.infrastructure.messaging.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when a budget is approved.
 * Part of the Saga choreography pattern.
 */
@Builder
public record BudgetApprovedEvent(
    String eventId,
    String budgetId,
    String serviceOrderId,
    String customerId,
    String vehicleId,
    BigDecimal totalAmount,
    LocalDateTime approvedAt,
    LocalDateTime timestamp
) {}

package br.com.techchallenge.fiap.billingservice.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for budget response.
 */
public record BudgetDto(
    String budgetId,
    String serviceOrderId,
    String customerId,
    String vehicleId,
    List<BudgetItemDto> items,
    BigDecimal totalAmount,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime approvedAt,
    LocalDateTime rejectedAt
) {}

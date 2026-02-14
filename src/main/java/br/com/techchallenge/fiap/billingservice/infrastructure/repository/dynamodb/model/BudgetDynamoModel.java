package br.com.techchallenge.fiap.billingservice.infrastructure.repository.dynamodb.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DynamoDB model for Budget.
 * This model represents how the Budget entity is stored in DynamoDB.
 */
@Builder
public record BudgetDynamoModel(
    String budgetId,
    String serviceOrderId,
    String customerId,
    String vehicleId,
    List<BudgetItemDynamoModel> items,
    BigDecimal totalAmount,
    String statusCode,
    String statusName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

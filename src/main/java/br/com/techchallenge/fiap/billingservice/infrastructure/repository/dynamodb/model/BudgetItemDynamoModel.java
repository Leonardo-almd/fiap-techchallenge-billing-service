package br.com.techchallenge.fiap.billingservice.infrastructure.repository.dynamodb.model;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * DynamoDB model for BudgetItem.
 */
@Builder
public record BudgetItemDynamoModel(
    String itemId,
    String type,
    String itemCode,
    String description,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal totalPrice
) {}

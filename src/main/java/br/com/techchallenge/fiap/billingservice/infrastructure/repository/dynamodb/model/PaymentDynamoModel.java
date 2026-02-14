package br.com.techchallenge.fiap.billingservice.infrastructure.repository.dynamodb.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DynamoDB model for Payment.
 * This model represents how the Payment entity is stored in DynamoDB.
 */
@Builder
public record PaymentDynamoModel(
    String paymentId,
    String budgetId,
    String serviceOrderId,
    BigDecimal amount,
    String methodCode,
    String methodName,
    String statusCode,
    String statusName,
    String externalId,
    String authorizationCode,
    String failureReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

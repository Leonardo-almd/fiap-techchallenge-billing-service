package br.com.techchallenge.fiap.billingservice.application.dto;

import br.com.techchallenge.fiap.billingservice.application.entity.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for payment response.
 */
public record PaymentDto(
    String paymentId,
    String budgetId,
    String serviceOrderId,
    BigDecimal amount,
    PaymentMethod method,
    String status,
    String externalId,
    String authorizationCode,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime processedAt,
    LocalDateTime refundedAt,
    String failureReason
) {}

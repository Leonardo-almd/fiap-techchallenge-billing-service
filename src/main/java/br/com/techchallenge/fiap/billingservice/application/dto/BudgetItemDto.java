package br.com.techchallenge.fiap.billingservice.application.dto;

import br.com.techchallenge.fiap.billingservice.application.entity.BudgetItemType;
import java.math.BigDecimal;

/**
 * DTO for budget item response.
 */
public record BudgetItemDto(
    String itemId,
    BudgetItemType type,
    String itemCode,
    String description,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal totalPrice
) {}

package br.com.techchallenge.fiap.billingservice.application.dto;

import br.com.techchallenge.fiap.billingservice.application.entity.BudgetItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * DTO for budget item creation request.
 */
public record BudgetItemRequestDto(
    @NotNull(message = "Item type is required")
    BudgetItemType type,
    
    @NotBlank(message = "Item code is required")
    String itemCode,
    
    @NotBlank(message = "Description is required")
    String description,
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    Integer quantity,
    
    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    BigDecimal unitPrice
) {}

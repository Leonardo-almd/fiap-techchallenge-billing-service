package br.com.techchallenge.fiap.billingservice.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * DTO for budget creation request.
 */
public record BudgetRequestDto(
    @NotBlank(message = "Service order ID is required")
    String serviceOrderId,
    
    @NotBlank(message = "Customer ID is required")
    String customerId,
    
    @NotBlank(message = "Vehicle ID is required")
    String vehicleId,
    
    @NotEmpty(message = "Budget must have at least one item")
    @Valid
    List<BudgetItemRequestDto> items
) {}

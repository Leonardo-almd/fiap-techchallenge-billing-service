package br.com.techchallenge.fiap.billingservice.application.dto;

import br.com.techchallenge.fiap.billingservice.application.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for payment processing request.
 */
public record PaymentRequestDto(
    @NotBlank(message = "Budget ID is required")
    String budgetId,
    
    @NotNull(message = "Payment method is required")
    PaymentMethod method
) {}

package br.com.techchallenge.fiap.billingservice.application.entity;

import br.com.techchallenge.fiap.billingservice.application.exception.InvalidDataException;
import java.util.Objects;

/**
 * Budget item entity representing a service or resource in the budget.
 * Immutable value object with business validations.
 */
public record BudgetItem(
    String itemId,
    BudgetItemType type,
    String itemCode,
    String description,
    Integer quantity,
    Price unitPrice,
    Price totalPrice
) {

    public BudgetItem {
        if (Objects.isNull(itemId) || itemId.isBlank()) {
            throw new InvalidDataException("BudgetItem itemId must not be null or blank");
        }
        if (Objects.isNull(type)) {
            throw new InvalidDataException("BudgetItem type must not be null");
        }
        if (Objects.isNull(itemCode) || itemCode.isBlank()) {
            throw new InvalidDataException("BudgetItem itemCode must not be null or blank");
        }
        if (Objects.isNull(description) || description.isBlank()) {
            throw new InvalidDataException("BudgetItem description must not be null or blank");
        }
        if (Objects.isNull(quantity) || quantity <= 0) {
            throw new InvalidDataException("BudgetItem quantity must be positive");
        }
        if (Objects.isNull(unitPrice)) {
            throw new InvalidDataException("BudgetItem unitPrice must not be null");
        }
        if (Objects.isNull(totalPrice)) {
            throw new InvalidDataException("BudgetItem totalPrice must not be null");
        }
        
        // Validar que totalPrice = unitPrice * quantity
        Price calculatedTotal = unitPrice.multiply(quantity);
        if (!totalPrice.value().equals(calculatedTotal.value())) {
            throw new InvalidDataException(
                "BudgetItem totalPrice must equal unitPrice * quantity"
            );
        }
    }

    /**
     * Calculate total price from unit price and quantity.
     */
    public static Price calculateTotal(Price unitPrice, int quantity) {
        return unitPrice.multiply(quantity);
    }
}
